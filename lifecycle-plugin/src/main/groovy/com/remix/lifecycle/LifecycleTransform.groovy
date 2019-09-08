package com.remix.lifecycle

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.IOUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

import static org.objectweb.asm.ClassReader.EXPAND_FRAMES

/**
 * created by Remix on 2019-09-06
 */
public class LifecycleTransform extends Transform {
    @Override
    String getName() {
        return "LifecyclePlugin"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        println "========LifecycleTransform start========"

        def startTime = System.currentTimeMillis()

        final Collection<TransformInput> inputs = transformInvocation.inputs
        final TransformOutputProvider outputProvider = transformInvocation.outputProvider

        // 删除之前的输出
        if (outputProvider != null) {
            outputProvider.deleteAll()
        }

        // 遍历inputs
        inputs.each {input ->

            // 遍历文件中的class
            input.directoryInputs.each {dirInput ->
                handleDirectoryInput(dirInput, outputProvider)
            }
            // 遍历jar中的class
            input.jarInputs.each {jarInput ->
                handleJarInput(jarInput, outputProvider)
            }
        }

        println "========LifecycleTransform end========"
        println "LifecycleTransform cost: ${System.currentTimeMillis() - startTime}"
    }

    private void handleDirectoryInput(DirectoryInput dirInput, TransformOutputProvider outputProvider) {
        if (dirInput.file.isDirectory()) {
            dirInput.file.eachFileRecurse {File file ->
                def name = file.name
                if (checkName(name)) {
                    println "------handle with class <${entryName}> in directory ${dirInput.file}"
                    ClassReader classReader = new ClassReader(file.bytes)
                    ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
                    ClassVisitor classVisitor = new LifecycleVisitor(classWriter)
                    classReader.accept(classVisitor, EXPAND_FRAMES)

                    byte codes = classWriter.toByteArray()
                    FileOutputStream fos = new FileOutputStream(file.parentFile.absolutePath + File.separator + name)
                    fos.write(codes)
                    fos.close()

                    println "------handle with class <${entryName}> end"
                }

                // 把输出传递给下一个任务
                def dest = outputProvider.getContentLocation(
                        dirInput.name,
                        dirInput.contentTypes,
                        dirInput.scopes,
                        Format.DIRECTORY)
                FileUtils.copyDirectory(dirInput.file, dest)
            }
        }
    }


    private void handleJarInput(JarInput jarInput, TransformOutputProvider outputProvider) {
        if (jarInput.file.getName().endsWith(".jar")) {
            def jarName = jarInput.name
            def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
            if (jarName.endsWith(".jar")) {
                jarName = jarName.substring(0, jarName.length() - 4)
            }


            JarFile jarFile = new JarFile(jarInput.file)
            Enumeration enumeration = jarFile.entries()
            File tmpFile = new File(jarInput.file.getParent() + File.separator + "classes_temp.jar")
            // 避免上次的缓存被重复输入
            if (tmpFile.exists()) {
                tmpFile.delete()
            }
            JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(tmpFile))
            // 保存
            while (enumeration.hasMoreElements()) {
                JarEntry jarEntry = enumeration.nextElement()
                String entryName = jarEntry.getName()
                ZipEntry zipEntry = new ZipEntry(entryName)
                InputStream inputStream = jarFile.getInputStream(jarEntry)
                if (checkName(entryName)) {
                    println "------handle with class <${entryName}> in jar ${jarName}"
                    jarOutputStream.putNextEntry(zipEntry)
                    // 插桩
                    ClassReader classReader = new ClassReader(IOUtils.toByteArray(inputStream))
                    ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
                    ClassVisitor classVisitor = new LifecycleVisitor(classWriter)
                    classReader.accept(classVisitor, EXPAND_FRAMES)
                    byte[] codes = classWriter.toByteArray()
                    jarOutputStream.write(codes)
                    println "------handle with class <${entryName}> end"
                } else {
                    // 不处理 直接输出
                    jarOutputStream.putNextEntry(zipEntry)
                    jarOutputStream.write(IOUtils.toByteArray(inputStream))
                }

                jarOutputStream.closeEntry()
            }
            // 结束
            jarOutputStream.close()
            jarFile.close()
            // 把输出传递给下一个任务
            def dest = outputProvider.getContentLocation(
                    jarName + md5Name,
                    jarInput.getContentTypes(),
                    jarInput.getScopes(),
                    Format.JAR
            )
            FileUtils.copyFile(tmpFile, dest)
            tmpFile.delete()
        }
    }

    public static boolean checkName(String entryName) {
        return ACTIVITY_NAMES.contains(entryName)
    }


    public static final List<String> ACTIVITY_NAMES = Arrays.asList(
            "androidx/fragment/app/FragmentActivity.class",
            "android/support/v4/app/FragmentActivity.class")
}
