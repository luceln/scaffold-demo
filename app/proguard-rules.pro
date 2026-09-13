# R8 混淆规则。Retrofit 与 kotlinx-serialization 都自带 consumer 规则
# （META-INF/proguard 随依赖自动合并），通常无需额外 keep。
# 这里只留骨架级的通用兜底；真正被 R8 报 missing class 时按报错加 -dontwarn，
# 别一上来就全量 keep（那等于放弃混淆）。

# kotlinx-serialization：保留编译器插件生成的 serializer 字段（官方建议的兜底）
-keepclassmembers class * implements kotlinx.serialization.KSerializer {
    *** Companion;
}

# 若引入的第三方库报 missing class，在此加对应的 -dontwarn 并写明原因：
