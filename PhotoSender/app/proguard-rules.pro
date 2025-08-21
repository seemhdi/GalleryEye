# Add project specific ProGuard rules here.
# You can find more information about how to configure ProGuard in the official documentation:
# https://www.guardsquare.com/en/products/proguard/manual/introduction

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Retain generic signatures of serializable classes.
-keepclassmembers,allowobfuscation class * extends java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
