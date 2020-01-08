package com.rxhttp.compiler

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import java.io.File
import java.io.IOException
import java.util.*
import javax.annotation.processing.Filer
import javax.lang.model.element.VariableElement
import javax.lang.model.util.Elements
import kotlin.Boolean
import kotlin.Long
import kotlin.String

class RxHttpGenerator {
    private var mParamsAnnotatedClass: ParamsAnnotatedClass? = null
    private var mParserAnnotatedClass: ParserAnnotatedClass? = null
    private var mDomainAnnotatedClass: DomainAnnotatedClass? = null
    private var mConverterAnnotatedClass: ConverterAnnotatedClass? = null
    private var defaultDomain: VariableElement? = null
    fun setAnnotatedClass(annotatedClass: ParamsAnnotatedClass?) {
        mParamsAnnotatedClass = annotatedClass
    }

    fun setAnnotatedClass(annotatedClass: ConverterAnnotatedClass?) {
        mConverterAnnotatedClass = annotatedClass
    }

    fun setAnnotatedClass(annotatedClass: DomainAnnotatedClass?) {
        mDomainAnnotatedClass = annotatedClass
    }

    fun setAnnotatedClass(annotatedClass: ParserAnnotatedClass?) {
        mParserAnnotatedClass = annotatedClass
    }

    fun setAnnotatedClass(defaultDomain: VariableElement?) {
        this.defaultDomain = defaultDomain
    }

    @Throws(IOException::class)
    fun generateCode(elementUtils: Elements?, filer: Filer?, platform: String) {
        val httpSenderName = ClassName("rxhttp", "HttpSender")
        val rxHttpPluginsName = ClassName("rxhttp", "RxHttpPlugins")
        val okHttpClientName = ClassName("okhttp3", "OkHttpClient")
        val schedulerName = ClassName("io.reactivex", "Scheduler")
        val converterName = ClassName("rxhttp.wrapper.callback", "IConverter")
        val schedulersName = ClassName("io.reactivex.schedulers", "Schedulers")
        val functionsName = ClassName("io.reactivex.functions", "Function")
        val jsonObjectName = ClassName("com.google.gson", "JsonObject")
        val jsonArrayName = ClassName("com.google.gson", "JsonArray")
        val stringName = String::class.asClassName()
        val objectName = Any::class.asClassName()
        val mapKVName = functionsName.parameterizedBy(paramPHName, paramPHName)
        val mapStringName = functionsName.parameterizedBy(stringName, stringName)
        val subObject = TypeVariableName("Any")
        val listName: TypeName = MutableList::class.asClassName().parameterizedBy(subObject)
        val listObjectName: TypeName = MutableList::class.asClassName().parameterizedBy(objectName)
        val t = TypeVariableName("T")
        //        TypeName typeName =  TypeName(String.class);
        val progressName = ClassName("rxhttp.wrapper.entity", "Progress")
        val progressTName: TypeName = progressName.parameterizedBy(t)
        val progressStringName: TypeName = progressName.parameterizedBy(stringName)
        val consumerName = ClassName("io.reactivex.functions", "Consumer")
        val observableName = ClassName("io.reactivex", "Observable")
        val consumerProgressStringName: TypeName = consumerName.parameterizedBy(progressStringName)
        val consumerProgressTName: TypeName = consumerName.parameterizedBy(progressTName)
        val parserName = ClassName("rxhttp.wrapper.parse", "Parser")
        val parserTName: TypeName = parserName.parameterizedBy(t)
        val observableTName: TypeName = observableName.parameterizedBy(t)
        val simpleParserName = ClassName("rxhttp.wrapper.parse", "SimpleParser")
        val upFileName = ClassName("rxhttp.wrapper.entity", "UpFile")
        val listUpFileName: TypeName = MutableList::class.asClassName().parameterizedBy(upFileName)
        val listFileName: TypeName = MutableList::class.asClassName().parameterizedBy(File::class.asTypeName())
        val subString = WildcardTypeName.producerOf(stringName)
        val mapName = MutableMap::class.asClassName().parameterizedBy(subString, TypeVariableName("*"))
        val noBodyParamName = ClassName(packageName, "NoBodyParam")
        val rxHttpNoBodyName = ClassName(packageName, "RxHttp_NoBodyParam")
        val formParamName = ClassName(packageName, "FormParam")
        val rxHttpFormName = ClassName(packageName, "RxHttp_FormParam")
        val jsonParamName = ClassName(packageName, "JsonParam")
        val rxHttpJsonName = ClassName(packageName, "RxHttp_JsonParam")
        val jsonArrayParamName = ClassName(packageName, "JsonArrayParam")
        val rxHttpJsonArrayName = ClassName(packageName, "RxHttp_JsonArrayParam")
        val rxHttpNoBody = RXHTTP.parameterizedBy(noBodyParamName, rxHttpNoBodyName)
        val rxHttpForm = RXHTTP.parameterizedBy(formParamName, rxHttpFormName)
        val rxHttpJson = RXHTTP.parameterizedBy(jsonParamName, rxHttpJsonName)
        val rxHttpJsonArray = RXHTTP.parameterizedBy(jsonArrayParamName, rxHttpJsonArrayName)
        val methodList: MutableList<FunSpec> = ArrayList() //方法集合
        val companionMethodList: MutableList<FunSpec> = ArrayList()
        var method = FunSpec.constructorBuilder()
            .addModifiers(KModifier.PROTECTED)
            .addParameter("param", p)
            .addStatement("this.param = param")
        methodList.add(method.build()) //添加构造方法
        method = FunSpec.builder("setDebug")
            .addModifiers(KModifier.PUBLIC)
            .addAnnotation(JvmStatic::class)
            .addParameter("debug", Boolean::class)
            .addStatement("%T.setDebug(debug)", httpSenderName)
        companionMethodList.add(method.build())
        method = FunSpec.builder("init")
            .addModifiers(KModifier.PUBLIC)
            .addAnnotation(JvmStatic::class)
            .addParameter("okHttpClient", okHttpClientName)
            .addStatement("%T.init(okHttpClient)", httpSenderName)
        companionMethodList.add(method.build())
        method = FunSpec.builder("init")
            .addModifiers(KModifier.PUBLIC)
            .addAnnotation(JvmStatic::class)
            .addParameter("okHttpClient", okHttpClientName)
            .addParameter("debug", Boolean::class)
            .addStatement("%T.init(okHttpClient,debug)", httpSenderName)
        companionMethodList.add(method.build())

        val annoDeprecated = AnnotationSpec.builder(Deprecated::class)
            .addMember(
                "\n\"please user {@link #setResultDecoder(Function)} instead\"," +
                    "\n    ReplaceWith(\"setResultDecoder(decoder)\", \"RxHttp.setResultDecoder\")")
            .build()
        method = FunSpec.builder("setOnConverter")
            .addAnnotation(JvmStatic::class)
            .addAnnotation(annoDeprecated)
            .addModifiers(KModifier.PUBLIC)
            .addKdoc("@deprecated please user {@link #setResultDecoder(Function)} instead\n")
            .addParameter("decoder", mapStringName)
            .addStatement("setResultDecoder(decoder)")
        companionMethodList.add(method.build())
        method = FunSpec.builder("setResultDecoder")
            .addModifiers(KModifier.PUBLIC)
            .addAnnotation(JvmStatic::class)
            .addKdoc("设置统一数据解码/解密器，每次请求成功后会回调该接口并传入Http请求的结果" +
                "\n通过该接口，可以统一对数据解密，并将解密后的数据返回即可" +
                "\n若部分接口不需要回调该接口，发请求前，调用{@link #setDecoderEnabled(boolean)}方法设置false即可\n")
            .addParameter("decoder", mapStringName)
            .addStatement("%T.setResultDecoder(decoder)", rxHttpPluginsName)
        companionMethodList.add(method.build())
        method = FunSpec.builder("setConverter")
            .addModifiers(KModifier.PUBLIC)
            .addAnnotation(JvmStatic::class)
            .addKdoc("设置全局转换器\n")
            .addParameter("globalConverter", converterName)
            .addStatement("%T.setConverter(globalConverter)", rxHttpPluginsName)
        companionMethodList.add(method.build())
        method = FunSpec.builder("setOnParamAssembly")
            .addModifiers(KModifier.PUBLIC)
            .addAnnotation(JvmStatic::class)
            .addKdoc("设置统一公共参数回调接口,通过该接口,可添加公共参数/请求头，每次请求前会回调该接口" +
                "\n若部分接口不需要添加公共参数,发请求前，调用{@link #setAssemblyEnabled(boolean)}方法设置false即可\n")
            .addParameter("onParamAssembly", mapKVName)
            .addStatement("%T.setOnParamAssembly(onParamAssembly)", rxHttpPluginsName)
        companionMethodList.add(method.build())
        method = FunSpec.builder("getOkHttpClient")
            .addModifiers(KModifier.PUBLIC)
            .addAnnotation(JvmStatic::class)
            .addStatement("return %T.getOkHttpClient()", httpSenderName)
        companionMethodList.add(method.build())
        method = FunSpec.builder("setParam")
            .addModifiers(KModifier.PUBLIC)
            .addParameter("param", p)
            .addStatement("this.param = param")
            .addStatement("return this as R")
            .returns(r)
        methodList.add(method.build())
        methodList.addAll(mParamsAnnotatedClass!!.getMethodList(filer, companionMethodList))
        methodList.addAll(mParserAnnotatedClass!!.getMethodList(platform))
        methodList.addAll(mConverterAnnotatedClass!!.methodList)
        method = FunSpec.builder("addDefaultDomainIfAbsent")
            .addModifiers(KModifier.PROTECTED)
            .addKdoc("给Param设置默认域名(如何缺席的话)，此方法会在请求发起前，被RxHttp内部调用\n")
            .addParameter("param", p)
        if (defaultDomain != null) {
            method.addStatement("val newUrl = addDomainIfAbsent(param.getSimpleUrl(), %T.%L)",
                defaultDomain!!.enclosingElement.asType().asTypeName(),
                defaultDomain!!.simpleName.toString())
                .addStatement("param.setUrl(newUrl)")
        }
        method.addStatement("return param")
            .returns(p)
        methodList.add(method.build())
        methodList.addAll(mDomainAnnotatedClass!!.getMethodList(companionMethodList))
        method = FunSpec.builder("format")
            .addModifiers(KModifier.PRIVATE)
            .addParameter("url", String::class)
            .addParameter("formatArgs", Any::class, KModifier.VARARG)
            .addStatement("return \n    if(formatArgs.size == 0) url else String.format(url, formatArgs)")
        companionMethodList.add(method.build())
        val schedulerField = PropertySpec.builder("scheduler", schedulerName.copy(nullable = true), KModifier.PROTECTED)
            .mutable()
            .initializer("%T.io()", schedulersName)
            .addKdoc("The request is executed on the IO thread by default\n")
            .build()
        val converterSpec = PropertySpec.builder("localConverter", converterName, KModifier.PROTECTED)
            .mutable()
            .initializer("%T.getConverter()", rxHttpPluginsName)
            .build()
        val paramSpec = PropertySpec.builder("param", p, KModifier.PROTECTED)
            .getter(FunSpec.getterBuilder()
                .addStatement("return field").build())
            .mutable()
            .build()

        val suppressAnno = AnnotationSpec.builder(Suppress::class)
            .addMember("\"UNCHECKED_CAST\"")
            .build()

        val companionType = TypeSpec.companionObjectBuilder()
            .addFunctions(companionMethodList)
            .build()
        val rxHttp = TypeSpec.classBuilder(CLASSNAME)
            .addKdoc("Github" +
                "\nhttps://github.com/liujingxing/RxHttp" +
                "\nhttps://github.com/liujingxing/RxLife\n")
            .addModifiers(KModifier.PUBLIC, KModifier.OPEN)
            .addAnnotation(suppressAnno)
            .addProperty(paramSpec)
            .addProperty(schedulerField)
            .addProperty(converterSpec)
            .addTypeVariable(p)
            .addTypeVariable(r)
            .addType(companionType)
            .addFunctions(methodList)
            .build()
        // Write file
        FileSpec.builder(packageName, "RxHttp")
            .addType(rxHttp)
            .build().writeTo(filer!!)

        //创建RxHttp_NoBodyParam类
        methodList.clear()
        method = FunSpec.constructorBuilder()
            .addModifiers(KModifier.PUBLIC)
            .addParameter("param", noBodyParamName)
            .callSuperConstructor("param")
        methodList.add(method.build())
        method = FunSpec.builder("add")
            .addModifiers(KModifier.PUBLIC)
            .addParameter("key", String::class)
            .addParameter("value", Any::class.asTypeName().copy(nullable = true))
            .addStatement("param.add(key,value)")
            .addStatement("return this")
            .returns(rxHttpNoBodyName)
        methodList.add(method.build())
        method = FunSpec.builder("addEncoded")
            .addModifiers(KModifier.PUBLIC)
            .addParameter("key", String::class)
            .addParameter("value", Any::class.asTypeName().copy(nullable = true))
            .addStatement("param.addEncoded(key,value)")
            .addStatement("return this")
            .returns(rxHttpNoBodyName)
        methodList.add(method.build())

        method = FunSpec.builder("add")
            .addModifiers(KModifier.PUBLIC)
            .addParameter("key", String::class)
            .addParameter("value", Any::class.asTypeName().copy(nullable = true))
            .addParameter("isAdd", Boolean::class)
            .beginControlFlow("if(isAdd)")
            .addStatement("param.add(key,value)")
            .endControlFlow()
            .addStatement("return this")
            .returns(rxHttpNoBodyName)
        methodList.add(method.build())
        method = FunSpec.builder("addAll")
            .addModifiers(KModifier.PUBLIC)
            .addParameter("map", mapName)
            .addStatement("param.addAll(map)")
            .addStatement("return this")
            .returns(rxHttpNoBodyName)
        methodList.add(method.build())
        method = FunSpec.builder("removeAllBody")
            .addModifiers(KModifier.PUBLIC)
            .addStatement("param.removeAllBody()")
            .addStatement("return this")
            .returns(rxHttpNoBodyName)
        methodList.add(method.build())
        method = FunSpec.builder("removeAllBody")
            .addModifiers(KModifier.PUBLIC)
            .addParameter("key", String::class)
            .addStatement("param.removeAllBody(key)")
            .addStatement("return this")
            .returns(rxHttpNoBodyName)
        methodList.add(method.build())
        method = FunSpec.builder("set")
            .addModifiers(KModifier.PUBLIC)
            .addParameter("key", String::class)
            .addParameter("value", Any::class.asTypeName().copy(nullable = true))
            .addStatement("param.set(key,value)")
            .addStatement("return this")
            .returns(rxHttpNoBodyName)
        methodList.add(method.build())
        method = FunSpec.builder("setEncoded")
            .addModifiers(KModifier.PUBLIC)
            .addParameter("key", String::class)
            .addParameter("value", Any::class.asTypeName().copy(nullable = true))
            .addStatement("param.setEncoded(key,value)")
            .addStatement("return this")
            .returns(rxHttpNoBodyName)
        methodList.add(method.build())
        method = FunSpec.builder("queryValue")
            .addModifiers(KModifier.PUBLIC)
            .addParameter("key", String::class)
            .addStatement("return param.queryValue(key)")
            .returns(Any::class.asTypeName().copy(nullable = true))
        methodList.add(method.build())
        method = FunSpec.builder("queryValues")
            .addModifiers(KModifier.PUBLIC)
            .addParameter("key", String::class)
            .addStatement("return param.queryValues(key)")
            .returns(listObjectName)
        methodList.add(method.build())
        val rxHttpNoBodySpec = TypeSpec.classBuilder("RxHttp_NoBodyParam")
            .addKdoc("Github" +
                "\nhttps://github.com/liujingxing/RxHttp" +
                "\nhttps://github.com/liujingxing/RxLife\n")
            .addModifiers(KModifier.PUBLIC, KModifier.OPEN)
            .superclass(rxHttpNoBody)
            .addFunctions(methodList)
            .build()
        FileSpec.builder(packageName, "RxHttp_NoBodyParam")
            .addType(rxHttpNoBodySpec)
            .build().writeTo(filer)


        //创建RxHttp_FormParam类
        methodList.clear()
        method = FunSpec.constructorBuilder()
            .addModifiers(KModifier.PUBLIC)
            .callSuperConstructor("param")
            .addParameter("param", formParamName)
        methodList.add(method.build())

        method = FunSpec.builder("add")
            .addModifiers(KModifier.PUBLIC)
            .addParameter("key", String::class)
            .addParameter("value", Any::class.asTypeName().copy(nullable = true))
            .addStatement("param.add(key,value)")
            .addStatement("return this")
            .returns(rxHttpFormName)
        methodList.add(method.build())
        method = FunSpec.builder("addEncoded")
            .addModifiers(KModifier.PUBLIC)
            .addParameter("key", String::class)
            .addParameter("value", Any::class.asTypeName().copy(nullable = true))
            .addStatement("param.addEncoded(key,value)")
            .addStatement("return this")
            .returns(rxHttpFormName)
        methodList.add(method.build())

        method = FunSpec.builder("add")
            .addModifiers(KModifier.PUBLIC)
            .addParameter("key", String::class)
            .addParameter("value", Any::class.asTypeName().copy(nullable = true))
            .addParameter("isAdd", Boolean::class)
            .beginControlFlow("if(isAdd)")
            .addStatement("param.add(key,value)")
            .endControlFlow()
            .addStatement("return this")
            .returns(rxHttpFormName)
        methodList.add(method.build())
        method = FunSpec.builder("addAll")
            .addModifiers(KModifier.PUBLIC)
            .addParameter("map", mapName)
            .addStatement("param.addAll(map)")
            .addStatement("return this")
            .returns(rxHttpFormName)
        methodList.add(method.build())
        method = FunSpec.builder("removeAllBody")
            .addModifiers(KModifier.PUBLIC)
            .addStatement("param.removeAllBody()")
            .addStatement("return this")
            .returns(rxHttpFormName)
        methodList.add(method.build())
        method = FunSpec.builder("removeAllBody")
            .addModifiers(KModifier.PUBLIC)
            .addParameter("key", String::class)
            .addStatement("param.removeAllBody(key)")
            .addStatement("return this")
            .returns(rxHttpFormName)
        methodList.add(method.build())
        method = FunSpec.builder("set")
            .addModifiers(KModifier.PUBLIC)
            .addParameter("key", String::class)
            .addParameter("value", Any::class.asTypeName().copy(nullable = true))
            .addStatement("param.set(key,value)")
            .addStatement("return this")
            .returns(rxHttpFormName)
        methodList.add(method.build())
        method = FunSpec.builder("setEncoded")
            .addModifiers(KModifier.PUBLIC)
            .addParameter("key", String::class)
            .addParameter("value", Any::class.asTypeName().copy(nullable = true))
            .addStatement("param.setEncoded(key,value)")
            .addStatement("return this")
            .returns(rxHttpFormName)
        methodList.add(method.build())
        method = FunSpec.builder("queryValue")
            .addModifiers(KModifier.PUBLIC)
            .addParameter("key", String::class)
            .addStatement("return param.queryValue(key)")
            .returns(Any::class.asClassName().copy(nullable = true))
        methodList.add(method.build())
        method = FunSpec.builder("queryValues")
            .addModifiers(KModifier.PUBLIC)
            .addParameter("key", String::class)
            .addStatement("return param.queryValues(key)")
            .returns(listObjectName)
        methodList.add(method.build())
        method = FunSpec.builder("add")
            .addModifiers(KModifier.PUBLIC)
            .addParameter("key", String::class)
            .addParameter("file", File::class.java)
            .addStatement("param.add(key,file)")
            .addStatement("return this")
            .returns(rxHttpFormName)
        methodList.add(method.build())
        method = FunSpec.builder("addFile")
            .addModifiers(KModifier.PUBLIC)
            .addParameter("key", String::class)
            .addParameter("file", File::class.java)
            .addStatement("param.addFile(key,file)")
            .addStatement("return this")
            .returns(rxHttpFormName)
        methodList.add(method.build())
        method = FunSpec.builder("addFile")
            .addModifiers(KModifier.PUBLIC)
            .addParameter("key", String::class)
            .addParameter("filePath", String::class)
            .addStatement("param.addFile(key,filePath)")
            .addStatement("return this")
            .returns(rxHttpFormName)
        methodList.add(method.build())
        method = FunSpec.builder("addFile")
            .addModifiers(KModifier.PUBLIC)
            .addParameter("key", String::class)
            .addParameter("value", String::class.asTypeName().copy(nullable = true))
            .addParameter("filePath", String::class)
            .addStatement("param.addFile(key,value,filePath)")
            .addStatement("return this")
            .returns(rxHttpFormName)
        methodList.add(method.build())
        method = FunSpec.builder("addFile")
            .addModifiers(KModifier.PUBLIC)
            .addParameter("key", String::class)
            .addParameter("value", String::class.asTypeName().copy(nullable = true))
            .addParameter("file", File::class.java)
            .addStatement("param.addFile(key,value,file)")
            .addStatement("return this")
            .returns(rxHttpFormName)
        methodList.add(method.build())
        method = FunSpec.builder("addFile")
            .addModifiers(KModifier.PUBLIC)
            .addParameter("file", upFileName)
            .addStatement("param.addFile(file)")
            .addStatement("return this")
            .returns(rxHttpFormName)
        methodList.add(method.build())
        method = FunSpec.builder("addFile")
            .addModifiers(KModifier.PUBLIC)
            .addParameter("key", String::class)
            .addParameter("fileList", listFileName)
            .addStatement("param.addFile(key,fileList)")
            .addStatement("return this")
            .returns(rxHttpFormName)
        methodList.add(method.build())
        method = FunSpec.builder("addFile")
            .addModifiers(KModifier.PUBLIC)
            .addParameter("fileList", listUpFileName)
            .addStatement("param.addFile(fileList)")
            .addStatement("return this")
            .returns(rxHttpFormName)
        methodList.add(method.build())
        method = FunSpec.builder("removeFile")
            .addModifiers(KModifier.PUBLIC)
            .addParameter("key", String::class)
            .addStatement("param.removeFile(key)")
            .addStatement("return this")
            .returns(rxHttpFormName)
        methodList.add(method.build())
        method = FunSpec.builder("setMultiForm")
            .addModifiers(KModifier.PUBLIC)
            .addStatement("param.setMultiForm()")
            .addStatement("return this")
            .returns(rxHttpFormName)
        methodList.add(method.build())
        method = FunSpec.builder("setUploadMaxLength")
            .addModifiers(KModifier.PUBLIC)
            .addParameter("maxLength", Long::class)
            .addStatement("param.setUploadMaxLength(maxLength)")
            .addStatement("return this")
            .returns(rxHttpFormName)
        methodList.add(method.build())
        method = FunSpec.builder("asUpload")
            .addModifiers(KModifier.PUBLIC)
            .addParameter("progressConsumer", consumerProgressStringName)
            .addStatement("return asUpload(%T(String::class.java), progressConsumer, null)", simpleParserName)
        methodList.add(method.build())
        method = FunSpec.builder("asUpload")
            .addModifiers(KModifier.PUBLIC)
            .addParameter("progressConsumer", consumerProgressStringName)
            .addParameter("observeOnScheduler", schedulerName)
            .addStatement("return asUpload(%T(String::class.java), progressConsumer, observeOnScheduler)", simpleParserName)
//            .returns(observableStringName)
        methodList.add(method.build())

        val parser = ParameterSpec.builder("parser", parserTName)
//            .defaultValue("SimpleParser.get(String::class.java")
            .build()

        val observeOnScheduler = ParameterSpec.builder("observeOnScheduler", schedulerName.copy(nullable = true))
//            .defaultValue("null")
            .build()
        method = FunSpec.builder("asUpload")
            .addModifiers(KModifier.PUBLIC)
            .addTypeVariable(t)
            .addParameter(parser)
            .addParameter("progressConsumer", consumerProgressTName)
            .addParameter(observeOnScheduler)
            .addStatement("setConverter(param)")
            .addStatement("var observable = %T\n" +
                ".uploadProgress(addDefaultDomainIfAbsent(param), parser, scheduler)", httpSenderName)
            .beginControlFlow("if(observeOnScheduler != null)")
            .addStatement("observable=observable.observeOn(observeOnScheduler)")
            .endControlFlow()
            .addStatement("return observable.doOnNext(progressConsumer)\n" +
                ".filter { it.isCompleted }\n" +
                ".map { it.result }")
            .returns(observableTName)
        methodList.add(method.build())
        val rxHttpFormSpec = TypeSpec.classBuilder("RxHttp_FormParam")
            .addKdoc("Github" +
                "\nhttps://github.com/liujingxing/RxHttp" +
                "\nhttps://github.com/liujingxing/RxLife\n")
            .addModifiers(KModifier.PUBLIC, KModifier.OPEN)
            .superclass(rxHttpForm)
            .addFunctions(methodList)
            .build()
        FileSpec.builder(packageName, "RxHttp_FormParam")
            .addType(rxHttpFormSpec)
            .build().writeTo(filer)

        //创建RxHttp_JsonParam类
        methodList.clear()
        method = FunSpec.constructorBuilder()
            .addModifiers(KModifier.PUBLIC)
            .addParameter("param", jsonParamName)
            .callSuperConstructor("param")
        methodList.add(method.build())
        method = FunSpec.builder("add")
            .addModifiers(KModifier.PUBLIC)
            .addParameter("key", String::class)
            .addParameter("value", Any::class.asTypeName().copy(nullable = true))
            .addStatement("param.add(key,value)")
            .addStatement("return this")
            .returns(rxHttpJsonName)
        methodList.add(method.build())
        method = FunSpec.builder("add")
            .addModifiers(KModifier.PUBLIC)
            .addParameter("key", String::class)
            .addParameter("value", Any::class.asTypeName().copy(nullable = true))
            .addParameter("isAdd", Boolean::class)
            .beginControlFlow("if(isAdd)")
            .addStatement("param.add(key,value)")
            .endControlFlow()
            .addStatement("return this")
            .returns(rxHttpJsonName)
        methodList.add(method.build())
        method = FunSpec.builder("addAll")
            .addModifiers(KModifier.PUBLIC)
            .addParameter("map", mapName)
            .addStatement("param.addAll(map)")
            .addStatement("return this")
            .returns(rxHttpJsonName)
        methodList.add(method.build())
        method = FunSpec.builder("addAll")
            .addModifiers(KModifier.PUBLIC)
            .addKdoc("将Json对象里面的key-value逐一取出，添加到另一个Json对象中，" +
                "\n输入非Json对象将抛出{@link IllegalStateException}异常\n")
            .addParameter("jsonObject", String::class)
            .addStatement("param.addAll(jsonObject)")
            .addStatement("return this")
            .returns(rxHttpJsonName)
        methodList.add(method.build())
        method = FunSpec.builder("addAll")
            .addModifiers(KModifier.PUBLIC)
            .addKdoc("将Json对象里面的key-value逐一取出，添加到另一个Json对象中\n")
            .addParameter("jsonObject", jsonObjectName)
            .addStatement("param.addAll(jsonObject)")
            .addStatement("return this")
            .returns(rxHttpJsonName)
        methodList.add(method.build())
        method = FunSpec.builder("addJsonElement")
            .addModifiers(KModifier.PUBLIC)
            .addKdoc("添加一个JsonElement对象(Json对象、json数组等)\n")
            .addParameter("key", String::class)
            .addParameter("jsonElement", String::class)
            .addStatement("param.addJsonElement(key,jsonElement)")
            .addStatement("return this")
            .returns(rxHttpJsonName)
        methodList.add(method.build())
        val rxHttpJsonSpec = TypeSpec.classBuilder("RxHttp_JsonParam")
            .addKdoc("Github" +
                "\nhttps://github.com/liujingxing/RxHttp" +
                "\nhttps://github.com/liujingxing/RxLife\n")
            .addModifiers(KModifier.PUBLIC, KModifier.OPEN)
            .superclass(rxHttpJson)
            .addFunctions(methodList)
            .build()
        FileSpec.builder(packageName, "RxHttp_JsonParam")
            .addType(rxHttpJsonSpec)
            .build().writeTo(filer)

        //创建RxHttp_JsonArrayParam类
        methodList.clear()
        method = FunSpec.constructorBuilder()
            .addModifiers(KModifier.PUBLIC)
            .addParameter("param", jsonArrayParamName)
            .callSuperConstructor("param")
        methodList.add(method.build())
        method = FunSpec.builder("add")
            .addModifiers(KModifier.PUBLIC)
            .addParameter("any", Any::class)
            .addStatement("param.add(any)")
            .addStatement("return this")
            .returns(rxHttpJsonArrayName)
        methodList.add(method.build())
        method = FunSpec.builder("add")
            .addModifiers(KModifier.PUBLIC)
            .addParameter("key", String::class)
            .addParameter("value", Any::class.asTypeName().copy(nullable = true))
            .addStatement("param.add(key,value)")
            .addStatement("return this")
            .returns(rxHttpJsonArrayName)
        methodList.add(method.build())
        method = FunSpec.builder("add")
            .addModifiers(KModifier.PUBLIC)
            .addParameter("key", String::class)
            .addParameter("value", Any::class.asTypeName().copy(nullable = true))
            .addParameter("isAdd", Boolean::class)
            .beginControlFlow("if(isAdd)")
            .addStatement("param.add(key,value)")
            .endControlFlow()
            .addStatement("return this")
            .returns(rxHttpJsonArrayName)
        methodList.add(method.build())
        method = FunSpec.builder("addAll")
            .addModifiers(KModifier.PUBLIC)
            .addParameter("map", mapName)
            .addStatement("param.addAll(map)")
            .addStatement("return this")
            .returns(rxHttpJsonArrayName)
        methodList.add(method.build())
        method = FunSpec.builder("addAll")
            .addModifiers(KModifier.PUBLIC)
            .addParameter("list", listName)
            .addStatement("param.addAll(list)")
            .addStatement("return this")
            .returns(rxHttpJsonArrayName)
        methodList.add(method.build())
        method = FunSpec.builder("addAll")
            .addModifiers(KModifier.PUBLIC)
            .addKdoc("添加多个对象，将字符串转JsonElement对象,并根据不同类型,执行不同操作,可输入任意非空字符串\n")
            .addParameter("jsonElement", String::class)
            .addStatement("param.addAll(jsonElement)")
            .addStatement("return this")
            .returns(rxHttpJsonArrayName)
        methodList.add(method.build())
        method = FunSpec.builder("addAll")
            .addModifiers(KModifier.PUBLIC)
            .addParameter("jsonArray", jsonArrayName)
            .addStatement("param.addAll(jsonArray)")
            .addStatement("return this")
            .returns(rxHttpJsonArrayName)
        methodList.add(method.build())
        method = FunSpec.builder("addAll")
            .addModifiers(KModifier.PUBLIC)
            .addKdoc("将Json对象里面的key-value逐一取出，添加到Json数组中，成为单独的对象\n")
            .addParameter("jsonObject", jsonObjectName)
            .addStatement("param.addAll(jsonObject)")
            .addStatement("return this")
            .returns(rxHttpJsonArrayName)
        methodList.add(method.build())
        method = FunSpec.builder("addJsonElement")
            .addModifiers(KModifier.PUBLIC)
            .addParameter("jsonElement", String::class)
            .addStatement("param.addJsonElement(jsonElement)")
            .addStatement("return this")
            .returns(rxHttpJsonArrayName)
        methodList.add(method.build())
        method = FunSpec.builder("addJsonElement")
            .addModifiers(KModifier.PUBLIC)
            .addKdoc("添加一个JsonElement对象(Json对象、json数组等)\n")
            .addParameter("key", String::class)
            .addParameter("jsonElement", String::class)
            .addStatement("param.addJsonElement(key,jsonElement)")
            .addStatement("return this")
            .returns(rxHttpJsonArrayName)
        methodList.add(method.build())
        val rxHttpJsonArraySpec = TypeSpec.classBuilder("RxHttp_JsonArrayParam")
            .addKdoc("Github" +
                "\nhttps://github.com/liujingxing/RxHttp" +
                "\nhttps://github.com/liujingxing/RxLife\n")
            .addModifiers(KModifier.PUBLIC, KModifier.OPEN)
            .superclass(rxHttpJsonArray)
            .addFunctions(methodList)
            .build()
        FileSpec.builder(packageName, "RxHttp_JsonArrayParam")
            .addType(rxHttpJsonArraySpec)
            .build().writeTo(filer)
    }

    companion object {
        private const val CLASSNAME = "RxHttp"
        const val packageName = "rxhttp.wrapper.param"
        @JvmField
        var RXHTTP = ClassName(packageName, CLASSNAME)
        private val P = TypeVariableName("P")
        private val R = TypeVariableName("R")
        private val placeHolder = TypeVariableName("*")
        private val paramName = ClassName(packageName, "Param")
        private val paramPName: TypeName = paramName.parameterizedBy(P)
        private val paramPHName: TypeName = paramName.parameterizedBy(placeHolder)
        private val rxHttpName = ClassName(packageName, CLASSNAME)
        private val rxHttpPRName = rxHttpName.parameterizedBy(P, R)
        @JvmField
        var p: TypeVariableName = TypeVariableName("P", paramPName)
        @JvmField
        var r: TypeVariableName = TypeVariableName("R", rxHttpPRName)
    }
}