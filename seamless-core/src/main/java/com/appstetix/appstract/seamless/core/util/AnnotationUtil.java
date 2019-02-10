package com.appstetix.appstract.seamless.core.util;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class AnnotationUtil {

    public static Set<String> findClassNamesWithAnnotation(Class annotation) {
        try (ScanResult scanResult = new ClassGraph().enableClassInfo().enableAnnotationInfo().scan()) {
            ClassInfoList classList = scanResult.getClassesWithAnnotation(annotation.getName());
            return classList != null && !classList.isEmpty() ? classList.stream().map(ClassInfo::getName).collect(Collectors.toSet()) : Collections.emptySet();
        } catch(Exception e) {
            e.printStackTrace();
            return Collections.EMPTY_SET;
        }
    }

    public static Set<Class<?>> findClassesWithAnnotation(Class annotation) {
        try (ScanResult scanResult = new ClassGraph().enableClassInfo().enableAnnotationInfo().scan()) {
            ClassInfoList classList = scanResult.getClassesWithAnnotation(annotation.getName());
            if(classList != null && !classList.isEmpty()) {
                Set<Class<?>> classes = new HashSet<>();
                classList.forEach(classInfo -> {
                    try {
                        classes.add(Class.forName(classInfo.getName()));
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                });
                return classes;
            }
            return Collections.EMPTY_SET;
        } catch(Exception e) {
            e.printStackTrace();
            return Collections.EMPTY_SET;
        }
    }

}
