/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
apply plugin: "maven"
apply plugin: "signing"

project.defaultTasks = ["uploadArchives"]
project.buildDir = 'bin/MAVEN'
project.group = "org.lwjgl"
project.version = lwjglVersion

// Set build variables based on build type (release, continuous integration, development)
enum BuildType {
    LOCAL,
    SNAPSHOT,
    RELEASE
}

class Deployment {
    BuildType type
    String repo
    String user
    String password
}

Deployment deployment
if (hasProperty("release")) {
    deployment = new Deployment(
        type: BuildType.RELEASE,
        repo: "https://oss.sonatype.org/service/local/staging/deploy/maven2/",
        user: sonatypeUsername,
        password: sonatypePassword
    )
} else if (hasProperty("snapshot")) {
    project.version += "-SNAPSHOT"
    deployment = new Deployment(
        type: BuildType.SNAPSHOT,
        repo: "https://oss.sonatype.org/content/repositories/snapshots/",
        user: sonatypeUsername,
        password: sonatypePassword
    )
} else {
    project.version += "-SNAPSHOT"
    deployment = new Deployment(
        type: BuildType.LOCAL,
        repo: repositories.mavenLocal().url
    )
}
println "${deployment.type.name()} BUILD"

enum Platforms {
    LINUX("linux"),
    LINUX_ARM64("linux-arm64"),
    LINUX_ARM32("linux-arm32"),
    MACOS("macos"),
    WINDOWS("windows"),
    WINDOWS_X86("windows-x86")

    static final Platforms[] JAVA_ONLY = []
    static final Platforms[] ALL = values()

    final String name

    Platforms(String name) {
        this.name = name
    }
}

enum Artifacts {
    CORE("lwjgl", "LWJGL", "The LWJGL core library.", Platforms.ALL),
    ASSIMP(
        "lwjgl-assimp", "LWJGL - Assimp bindings",
        "A portable Open Source library to import various well-known 3D model formats in a uniform manner.",
        Platforms.ALL
    ),
    BGFX(
        "lwjgl-bgfx", "LWJGL - bgfx bindings",
        "A cross-platform, graphics API agnostic rendering library. It provides a high performance, low level abstraction for common platform graphics APIs like OpenGL, Direct3D and Apple Metal.",
        Platforms.ALL
    ),
    BULLET(
        "lwjgl-bullet", "LWJGL - Bullet bindings",
        "Real-time collision detection and multi-physics simulation for VR, games, visual effects, robotics, machine learning etc.",
        Platforms.ALL
    ),
    CUDA(
        "lwjgl-cuda", "LWJGL - CUDA bindings",
        "A parallel computing platform and programming model developed by NVIDIA for general computing on GPUs.",
        Platforms.JAVA_ONLY
    ),
    EGL(
        "lwjgl-egl", "LWJGL - EGL bindings",
        "An interface between Khronos rendering APIs such as OpenGL ES or OpenVG and the underlying native platform window system.",
        Platforms.JAVA_ONLY
    ),
    GLFW(
        "lwjgl-glfw", "LWJGL - GLFW bindings",
        "A multi-platform library for OpenGL, OpenGL ES and Vulkan development on the desktop. It provides a simple API for creating windows, contexts and surfaces, receiving input and events.",
        Platforms.ALL
    ),
    JAWT(
        "lwjgl-jawt", "LWJGL - JAWT bindings",
        "The AWT native interface.",
        Platforms.JAVA_ONLY
    ),
    JEMALLOC(
        "lwjgl-jemalloc", "LWJGL - jemalloc bindings",
        "A general purpose malloc implementation that emphasizes fragmentation avoidance and scalable concurrency support.",
        Platforms.ALL
    ),
    LIBDIVIDE(
        "lwjgl-libdivide", "LWJGL - libdivide bindings",
        "A library that replaces expensive integer divides with comparatively cheap multiplication and bitshifts.",
        Platforms.ALL
    ),
    LLVM(
        "lwjgl-llvm", "LWJGL - LLVM/Clang bindings",
        "A collection of modular and reusable compiler and toolchain technologies.",
        Platforms.ALL
    ),
    LMDB(
        "lwjgl-lmdb", "LWJGL - LMDB bindings",
        "A compact, fast, powerful, and robust database that implements a simplified variant of the BerkeleyDB (BDB) API.",
        Platforms.ALL
    ),
    LZ4(
        "lwjgl-lz4", "LWJGL - LZ4 bindings",
        "A lossless data compression algorithm that is focused on compression and decompression speed.",
        Platforms.ALL
    ),
    MEOW(
        "lwjgl-meow", "LWJGL - Meow hash bindings",
        "An extremely fast non-cryptographic hash.",
        Platforms.LINUX, Platforms.LINUX_ARM64, Platforms.MACOS, Platforms.WINDOWS, Platforms.WINDOWS_X86
    ),
    NANOVG(
        "lwjgl-nanovg", "LWJGL - NanoVG & NanoSVG bindings",
        "A small antialiased vector graphics rendering library for OpenGL. Also includes NanoSVG, a simple SVG parser.",
        Platforms.ALL
    ),
    NFD(
        "lwjgl-nfd", "LWJGL - Native File Dialog bindings",
        "A tiny, neat C library that portably invokes native file open and save dialogs.",
        Platforms.LINUX, Platforms.MACOS, Platforms.WINDOWS, Platforms.WINDOWS_X86
    ),
    NUKLEAR(
        "lwjgl-nuklear", "LWJGL - Nuklear bindings",
        "A minimal state immediate mode graphical user interface toolkit.",
        Platforms.ALL
    ),
    ODBC(
        "lwjgl-odbc", "LWJGL - ODBC bindings",
        "A C programming language interface that makes it possible for applications to access data from a variety of database management systems (DBMSs).",
        Platforms.JAVA_ONLY
    ),
    OPENAL(
        "lwjgl-openal", "LWJGL - OpenAL bindings",
        "A cross-platform 3D audio API appropriate for use with gaming applications and many other types of audio applications.",
        Platforms.ALL
    ),
    OPENCL(
        "lwjgl-opencl", "LWJGL - OpenCL bindings",
        "An open, royalty-free standard for cross-platform, parallel programming of diverse processors found in personal computers, servers, mobile devices and embedded platforms.",
        Platforms.JAVA_ONLY
    ),
    OPENGL(
        "lwjgl-opengl", "LWJGL - OpenGL bindings",
        "The most widely adopted 2D and 3D graphics API in the industry, bringing thousands of applications to a wide variety of computer platforms.",
        Platforms.ALL
    ),
    OPENGLES(
        "lwjgl-opengles", "LWJGL - OpenGL ES bindings",
        "A royalty-free, cross-platform API for full-function 2D and 3D graphics on embedded systems - including consoles, phones, appliances and vehicles.",
        Platforms.ALL
    ),
    OPENVR(
        "lwjgl-openvr", "LWJGL - OpenVR bindings",
        "An API and runtime that allows access to VR hardware from multiple vendors without requiring that applications have specific knowledge of the hardware they are targeting.",
        Platforms.LINUX, Platforms.MACOS, Platforms.WINDOWS, Platforms.WINDOWS_X86
    ),
    OPUS(
        "lwjgl-opus", "LWJGL - Opus bindings",
        "A totally open, royalty-free, highly versatile audio codec.",
        Platforms.ALL
    ),
    OVR(
        "lwjgl-ovr", "LWJGL - OVR bindings",
        "The API of the Oculus SDK.",
        Platforms.WINDOWS, Platforms.WINDOWS_X86
    ),
    PAR(
        "lwjgl-par", "LWJGL - par_shapes bindings",
        "Generate parametric surfaces and other simple shapes.",
        Platforms.ALL
    ),
    REMOTERY(
        "lwjgl-remotery", "LWJGL - Remotery bindings",
        "A realtime CPU/GPU profiler hosted in a single C file with a viewer that runs in a web browser.",
        Platforms.ALL
    ),
    RPMALLOC(
        "lwjgl-rpmalloc", "LWJGL - rpmalloc bindings",
        "A public domain cross platform lock free thread caching 16-byte aligned memory allocator implemented in C.",
        Platforms.ALL
    ),
    SHADERC(
        "lwjgl-shaderc", "LWJGL - Shaderc bindings",
        "A collection of libraries for shader compilation.",
        Platforms.ALL
    ),
    SSE(
        "lwjgl-sse", "LWJGL - SSE bindings",
        "Simple SSE intrinsics.",
        Platforms.LINUX, Platforms.MACOS, Platforms.WINDOWS, Platforms.WINDOWS_X86
    ),
    STB(
        "lwjgl-stb", "LWJGL - stb bindings",
        "Single-file public domain libraries for fonts, images, ogg vorbis files and more.",
        Platforms.ALL
    ),
    TINYEXR(
        "lwjgl-tinyexr", "LWJGL - Tiny OpenEXR bindings",
        "A small library to load and save OpenEXR(.exr) images.",
        Platforms.ALL
    ),
    TINYFD(
        "lwjgl-tinyfd", "LWJGL - Tiny File Dialogs bindings",
        "Provides basic modal dialogs.",
        Platforms.ALL
    ),
    TOOTLE(
        "lwjgl-tootle", "LWJGL - AMD Tootle bindings",
        "A 3D triangle mesh optimization library that improves on existing mesh preprocessing techniques.",
        Platforms.LINUX, Platforms.MACOS, Platforms.WINDOWS, Platforms.WINDOWS_X86
    ),
    VMA(
        "lwjgl-vma", "LWJGL - Vulkan Memory Allocator bindings",
        "An easy to integrate Vulkan memory allocation library.",
        Platforms.ALL
    ),
    VULKAN(
        "lwjgl-vulkan", "LWJGL - Vulkan bindings",
        "A new generation graphics and compute API that provides high-efficiency, cross-platform access to modern GPUs used in a wide variety of devices from PCs and consoles to mobile phones and embedded platforms.",
        Platforms.MACOS
    ),
    XXHASH(
        "lwjgl-xxhash", "LWJGL - xxHash bindings",
        "An extremely fast hash algorithm, running at RAM speed limits.",
        Platforms.ALL
    ),
    YOGA(
        "lwjgl-yoga", "LWJGL - Yoga bindings",
        "An open-source, cross-platform layout library that implements Flexbox.",
        Platforms.ALL
    ),
    ZSTD(
        "lwjgl-zstd", "LWJGL - Zstandard bindings",
        "A fast lossless compression algorithm, targeting real-time compression scenarios at zlib-level and better compression ratios.",
        Platforms.ALL
    )

    String artifact
    String projectName
    String projectDescription
    Platforms[] platforms

    private Artifacts(
        String artifact,
        String projectName, String projectDescription,
        Platforms... platforms
    ) {
        this.artifact = artifact
        this.projectName = projectName
        this.projectDescription = projectDescription
        this.platforms = platforms
    }

    private String directory(String buildDir) {
        return "$buildDir/$artifact"
    }

    private String path() {
        return "${directory("bin/MAVEN")}/$artifact"
    }

    boolean active() {
        return new File(directory("bin/RELEASE")).exists()
    }

    boolean hasArtifact(String classifier) {
        return new File("${directory("bin/RELEASE")}/${artifact}-${classifier}.jar" as String).exists()
    }

    Map<String, ? extends Object> artifactNotation(String classifier = null) {
        return classifier == null
            ? [file: new File("${path()}.jar" as String), name: artifact, type: "jar"]
            : [file: new File("${path()}-${classifier}.jar" as String), name: artifact, type: "jar", classifier: classifier]
    }
}

artifacts {
    /*
    Ideally, we'd have the following structure:
    -------------------------------------------
    lwjgl
        lwjgl-windows (depends on lwjgl)
    glfw (depends on lwjgl)
        glfw-windows (depends on glfw & lwjgl-windows)
    stb (depends on lwjgl)
        stb-windows (depends on stb & lwjgl-windows)
    -------------------------------------------
    If a user wanted to use GLFW + stb in their project, running on
    the Windows platform, they'd only have to define glfw-windows
    and stb-windows as dependencies. This would automatically
    resolve stb, glfw, lwjgl and lwjgl-windows as transitive
    dependencies. Unfortunately, it is not possible to define such
    a relationship between Maven artifacts when using classifiers.

    A method to make this work is make the natives-<arch> classified
    JARs separate artifacts. We do not do it for aesthetic reasons.

    Instead, we assume that a tool is available (on the LWJGL website)
    that automatically generates POM/Gradle dependency structures for
    projects wanting to use LWJGL. The output is going to be verbose;
    the above example is going to look like this in Gradle:
    -------------------------------------------
    compile 'org.lwjgl:lwjgl:$lwjglVersion' // NOTE: this is optional, all binding artifacts have a dependency on lwjgl
        compile 'org.lwjgl:lwjgl:$lwjglVersion:natives-$lwjglArch'
    compile 'org.lwjgl:lwjgl-glfw:$lwjglVersion'
        compile 'org.lwjgl:lwjgl-glfw:$lwjglVersion:natives-$lwjglArch'
    compile 'org.lwjgl:lwjgl-stb:$lwjglVersion'
        compile 'org.lwjgl:lwjgl-stb:$lwjglVersion:natives-$lwjglArch'
    -------------------------------------------
    and a whole lot more verbose in Maven. Hopefully, the automation
    is going to alleviate the pain.
     */
    Artifacts.values().each { module ->
        if (module.active()) {
            archives module.artifactNotation()
            if (deployment.type != BuildType.LOCAL || module.hasArtifact("sources")) {
                archives module.artifactNotation("sources")
            }
            if (deployment.type != BuildType.LOCAL || module.hasArtifact("javadoc")) {
                archives module.artifactNotation("javadoc")
            }
            module.platforms.each {
                if (deployment.type != BuildType.LOCAL || module.hasArtifact("natives-${it.name}")) {
                    archives module.artifactNotation("natives-${it.name}")
                }
            }
        }
    }
}

signing {
    required = deployment.type == BuildType.RELEASE
    sign configurations.archives
}
signArchives.dependsOn "copyArchives"
uploadArchives.dependsOn signArchives

// TODO: Find a way to merge the POM generation closures
def lwjglPOM = { String projectName, String projectDescription ->
    return {
        project {
            name projectName
            description projectDescription
            url 'https://www.lwjgl.org'

            scm {
                connection 'scm:git:https://github.com/LWJGL/lwjgl3.git'
                developerConnection 'scm:git:https://github.com/LWJGL/lwjgl3.git'
                url 'https://github.com/LWJGL/lwjgl3.git'
            }

            licenses {
                license {
                    name 'BSD'
                    url 'https://www.lwjgl.org/license'
                    distribution 'repo'
                }
            }

            developers {
                developer {
                    id "spasi"
                    name "Ioannis Tsakpinis"
                    email "iotsakp@gmail.com"
                    url "https://github.com/Spasi"
                }
            }
        }
    }
}

def bindingPOM = { String projectName, String projectDescription ->
    return {
        project {
            name projectName
            description projectDescription
            url 'https://www.lwjgl.org'

            scm {
                connection 'scm:git:https://github.com/LWJGL/lwjgl3.git'
                developerConnection 'scm:git:https://github.com/LWJGL/lwjgl3.git'
                url 'https://github.com/LWJGL/lwjgl3.git'
            }

            licenses {
                license {
                    name 'BSD'
                    url 'https://www.lwjgl.org/license'
                    distribution 'repo'
                }
            }

            developers {
                developer {
                    id "spasi"
                    name "Ioannis Tsakpinis"
                    email "iotsakp@gmail.com"
                    url "https://github.com/Spasi"
                }
            }

            dependencies {
                dependency {
                    groupId 'org.lwjgl'
                    artifactId 'lwjgl'
                    version project.version
                    scope 'compile'
                }
            }
        }
    }
}

uploadArchives {
    repositories {
        mavenDeployer {
            repository(url: deployment.repo) {
                authentication(userName: deployment.user, password: deployment.password)
            }

            beforeDeployment {
                signing.signPom(it)
            }

            Artifacts.values().each {
                addFilter(it.artifact) {
                    artifact, file -> artifact.name == it.artifact
                }

                pom(
                    it.artifact,
                    it == Artifacts.CORE
                        ? lwjglPOM(it.projectName, it.projectDescription)
                        : bindingPOM(it.projectName, it.projectDescription)
                )
            }
        }
    }
}

task copyArchives(type: Copy) {
    from "bin/RELEASE"
    include "**"
    destinationDir buildDir
}