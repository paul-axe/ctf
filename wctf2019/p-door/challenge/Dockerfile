FROM php:7-apache

RUN mv "$PHP_INI_DIR/php.ini-production" "$PHP_INI_DIR/php.ini"
RUN yes '' | pecl install redis
Run echo 'extension=redis.so' >> "$PHP_INI_DIR/php.ini"
RUN mkdir /tmp/cache/ && chown www-data:www-data /tmp/cache

COPY src/ /var/www/html
# So teams will know where is the flag
COPY docker-compose.yaml / 

