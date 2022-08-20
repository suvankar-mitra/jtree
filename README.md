![build](https://github.com/suvankar-mitra/jtree/actions/workflows/maven.yml/badge.svg)

# jtree
A Linux 'tree' command clone written in Kotlin / Java.

![jtree-output](jtree-output.png)

## How to use it?
Prerequisites:
- Clone this project and then run ```jtree``` script. 
- The script will first check if you have Java runtime installed or not.
If Java is not installed in your system, please install it and make sure ```java -version``` returns java version.
- In the generated `target` folder, you will get the fat jar: `jtree-<version>-jar-with-dependencies.jar`
- Use this fat jar in your project classpath

### Java
```java
import com.suvmitra.jtree.JTree;

class Demo {
    public static void main(String[] args) {
        JTree jTree = new JTree();
        jTree.traverseDir("."); // traverse current directory
        System.out.println(jTree.getTreeOutput()); // display the tree
        System.out.println(jTree.getTreeOutputMeta()); // display the metadata
    }
}
```

### Kotlin

```kotlin

import javax.swing.JTree

fun main() {
    val jTree = JTree
    jTree.traverseDir(".") // traverse current directory
    println(jTree.getTreeOutput()) // display the tree
    println(jTree.getTreeOutputMeta()) // display the metadata
}
```

## License
[MIT](https://choosealicense.com/licenses/mit/)