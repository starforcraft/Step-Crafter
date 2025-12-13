plugins {
    id("com.refinedmods.refinedarchitect.common")
}

repositories {
    maven {
        name = "Refined Storage"
        url = uri("https://maven.creeperhost.net")
        content {
            includeGroup("com.refinedmods.refinedstorage")
        }
    }
    maven {
        name = "JEI"
        url = uri("https://maven.blamejared.com/")
    }
    maven {
        name = "EMI"
        url = uri("https://maven.terraformersmc.com/")
    }
}

val modVersion: String by project

refinedarchitect {
    version = modVersion
    common()
    publishing {
        maven = true
    }
}

base {
    archivesName.set("stepcrafter-common")
}

val refinedstorageVersion: String by project
val refinedstorageQuartzArsenalVersion: String by project
val jeiVersion: String by project
val emiVersion: String by project
val minecraftVersion: String by project

dependencies {
    api("com.refinedmods.refinedstorage:refinedstorage-common:${refinedstorageVersion}")
    api("com.refinedmods.refinedstorage:refinedstorage-quartz-arsenal-common:${refinedstorageQuartzArsenalVersion}")

    api("mezz.jei:jei-${minecraftVersion}-common-api:${jeiVersion}")
    api("mezz.jei:jei-${minecraftVersion}-common:${jeiVersion}")
    compileOnlyApi("dev.emi:emi-xplat-mojmap:${emiVersion}")
    compileOnly("io.github.llamalad7:mixinextras-common:0.4.1")
}
