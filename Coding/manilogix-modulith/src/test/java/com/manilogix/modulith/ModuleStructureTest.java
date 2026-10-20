package com.manilogix.modulith;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
class ModuleStructureTest {
 @Test void verifiesModuleBoundaries() {
  ApplicationModules.of(ManiLogixApplication.class).verify();
 }
}

