package com.example.auth.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class AuthModuleArchitectureTest {

    private static final String BASE_PACKAGE = "com.example.auth";

    @Test
    @DisplayName("auth 모듈은 api/web 계층에 의존하지 않는다")
    void authModuleShouldNotDependOnApiLayer() {
        JavaClasses imported = new ClassFileImporter().importPackages(BASE_PACKAGE);

        ArchRule rule = noClasses()
                .that().resideInAnyPackage("com.example.auth..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("com.example.api..");

        rule.allowEmptyShould(true).check(imported);
    }
}
