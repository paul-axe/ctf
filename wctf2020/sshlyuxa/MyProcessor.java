import com.sun.tools.javac.comp.Attr;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeTranslator;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Set;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("*")
public class MyProcessor  extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element e : roundEnv.getRootElements()) {
            if (e.getSimpleName().contentEquals("A")) {
                JavacElements utils = (JavacElements) processingEnv.getElementUtils();
                JCTree.JCClassDecl cls = (JCTree.JCClassDecl) utils.getTree(e);
                JCTree.JCMethodDecl m = (JCTree.JCMethodDecl) cls.defs.head;
                cls.defs.head = new JCTree.JCMethodDecl(m.mods, m.name, m.restype, m.typarams, m.recvparam, m.params, m.thrown, m.body, m.defaultValue, m.sym) {
                    @Override
                    public void accept(Visitor v) {
                        if (v instanceof Attr || v instanceof TreeTranslator) {
                            super.accept(v);
                        }
                    }
                };
            }
        }
        return true;
    }
}
