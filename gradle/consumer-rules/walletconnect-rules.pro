-keep class io.crosstoken.android.** { *; }
-keep interface com.walletconnect.** { *; }
-keep interface io.crosstoken.** { *; }
-keep class kotlinx.coroutines.** { *; }

-dontwarn kotlinx.coroutines.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
-dontwarn okhttp3.internal.platform.**

-allowaccessmodification
-keeppackagenames doNotKeepAThing

-dontwarn groovy.lang.GroovyShell
