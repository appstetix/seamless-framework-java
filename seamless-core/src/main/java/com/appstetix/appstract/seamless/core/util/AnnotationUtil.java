package com.appstetix.appstract.seamless.core.util;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class AnnotationUtil {

    public static Set<String> findClassesWithAnnotation(Class annotation) {
        try (ScanResult scanResult = new ClassGraph().enableClassInfo().enableAnnotationInfo().scan()) {
            ClassInfoList classList = scanResult.getClassesWithAnnotation(annotation.getName());
            return classList != null && !classList.isEmpty() ? classList.stream().map(ClassInfo::getName).collect(Collectors.toSet()) : null;
        } catch(Exception e) {
            e.printStackTrace();
            return Collections.EMPTY_SET;
        }
    }

}
