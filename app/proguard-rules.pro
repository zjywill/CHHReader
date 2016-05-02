# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/zhangjunyi/Documents/Develop/androidSDK/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

#------------------------------------------------

-dontwarn android.util.FloatMath
-keep class android.util.FloatMath

#-------------------persistentcookiejar-----------------------------

-dontwarn co.moonmonkeylabs.realmrecyclerview.**
-keep class co.moonmonkeylabs.realmrecyclerview.**

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
#-------------------persistentcookiejar-----------------------------

#------retrofit begin----------------
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions
#------retrofit end------------------

#------realm begin----------------
-keep class io.realm.RealmCollection
-keep class io.realm.OrderedRealmCollection
#------realm end------------------


#------okhttp begin----------------
-keep class com.squareup.okhttp.**
-dontwarn com.squareup.okhttp.**
#------okhttp end------------------

#------google begin----------------
-keep class com.google.**
-dontwarn com.google.**
#------google end----------------

-keep class java.nio.**
-dontwarn java.nio.**

-keep class sun.misc.**
-dontwarn sun.misc.**

-keep class org.codehaus.mojo.**
-dontwarn org.codehaus.mojo.**

-keep class android.net.**
-dontwarn android.net.**

-keep class org.jsoup.**
-dontwarn org.jsoup.**


#------rxjava rxandroid begin----------------
-dontwarn sun.misc.**

-keep class rx.schedulers.Schedulers {
    public static <methods>;
}
-keep class rx.schedulers.ImmediateScheduler {
    public <methods>;
}
-keep class rx.schedulers.TestScheduler {
    public <methods>;
}
-keep class rx.schedulers.Schedulers {
    public static ** test();
}
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
    long producerIndex;
    long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    long producerNode;
    long consumerNode;
}
#------rxjava rxandroid end----------------

#------gradle-retrolambda----
-dontwarn java.lang.invoke.*
