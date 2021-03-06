SUMMARY = "WebKit for Wayland port pairs the WebKit engine with the Wayland display protocol, \
           allowing embedders to create simple and performant systems based on Web platform technologies. \
           It is designed with hardware acceleration in mind, relying on EGL, the Wayland EGL platform, and OpenGL ES."
HOMEPAGE = "http://www.webkitforwayland.org/"
LICENSE = "BSD & LGPLv2+"
LIC_FILES_CHKSUM = "file://Source/WebCore/LICENSE-LGPL-2.1;md5=a778a33ef338abbaf8b8a7c36b6eec80 "

# you need harfbuzz with icu enabled, you can add this to your config:
# PACKAGECONFIG_append_pn-harfbuzz = " icu" if you are having problems
# with the do_configure step and harfbuzz.
DEPENDS = "zlib enchant libsoup-2.4 curl libxml2 cairo libxslt libidn libgcrypt \
           gtk+3 gstreamer1.0 gstreamer1.0-plugins-base flex-native icu \
           gperf-native perl-native ruby-native sqlite3 \
           libwebp harfbuzz virtual/libgles2 wpebackend glib-2.0-native"


REQUIRED_DISTRO_FEATURES = "wayland"

inherit cmake pkgconfig perlnative pythonnative



#
# We download a tarball from github instead of cloning the upstream git repository
# because requires less resources (network bandwidth and disk space) on the build machine.
#

SVNREV = "222017"
GITHASH = "be476896877f8e2efa98486fb398e374ea900425"

S = "${WORKDIR}/webkit-${GITHASH}/"
PV = "svn-${SVNREV}"

SRC_URI = "\
   https://github.com/webkit/webkit/archive/${GITHASH}.tar.gz \
"

SRC_URI[md5sum] = "de1391d7286a466176f90f25f0ca50bc"
SRC_URI[sha256sum] = "d1fa8cdfc7f075144b6bb8df8ea2fb92b102ca1e05433b272ed3a78d358c219f"

EXTRA_OECMAKE = " \
                 -DPORT=WPE \
                 -DCMAKE_BUILD_TYPE=Release \
                "


PACKAGECONFIG ?= "webcrypto gst_gl"
PACKAGECONFIG[webcrypto] = "-DENABLE_WEB_CRYPTO=ON,-DENABLE_WEB_CRYPTO=OFF,libgcrypt libtasn1"
PACKAGECONFIG[gst_gl] = "-DUSE_GSTREAMER_GL=ON,-DUSE_GSTREAMER_GL=OFF,gstreamer1.0-plugins-bad"


# Javascript JIT is not supported on powerpc
EXTRA_OECMAKE_append_powerpc = " -DENABLE_JIT=OFF "
EXTRA_OECMAKE_append_powerpc64 = " -DENABLE_JIT=OFF "

# ARM JIT code does not build on ARMv5/6 anymore, apparently they test only on v7 onwards
EXTRA_OECMAKE_append_armv4 = " -DENABLE_JIT=OFF "
EXTRA_OECMAKE_append_armv5 = " -DENABLE_JIT=OFF "
EXTRA_OECMAKE_append_armv6 = " -DENABLE_JIT=OFF "

# binutils 2.25.1 has a bug on aarch64:
# https://sourceware.org/bugzilla/show_bug.cgi?id=18430
EXTRA_OECMAKE_append_aarch64 = " -DUSE_LD_GOLD=OFF "

# JIT not supported on MIPS either
EXTRA_OECMAKE_append_mips = " -DENABLE_JIT=OFF "
EXTRA_OECMAKE_append_mips64 = " -DENABLE_JIT=OFF "

# http://errors.yoctoproject.org/Errors/Details/20370/
ARM_INSTRUCTION_SET_armv4 = "arm"
ARM_INSTRUCTION_SET_armv5 = "arm"
ARM_INSTRUCTION_SET_armv6 = "arm"

# https://bugzilla.yoctoproject.org/show_bug.cgi?id=9474
# https://bugs.webkit.org/show_bug.cgi?id=159880
# JSC JIT can build on ARMv7 with -marm, but doesn't work on runtime.
# Upstream only tests regularly the JSC JIT on ARMv7 with Thumb2 (-mthumb).
ARM_INSTRUCTION_SET_armv7a = "thumb"
ARM_INSTRUCTION_SET_armv7r = "thumb"
ARM_INSTRUCTION_SET_armv7m = "thumb"
ARM_INSTRUCTION_SET_armv7ve = "thumb"

# We manually set the includes files for the binary and dev package here,
# (overriding the default settings) because some libraries (libWPE and
# libWPEWebInspectorResources) are not versioned, so we must include
# the .so file in the binary package instead of the dev one.
FILES_${PN}-dev = " \
${includedir} \
${libdir}/libWPE.so \
${libdir}/libWPEWebKit.so \
${libdir}/pkgconfig \
"
FILES_${PN} = " \
${bindir} \
${libdir}/libWPE.so.* \
${libdir}/libWPEWebInspectorResources.so \
${libdir}/libWPEWebKit.so.* \
"

RRECOMMENDS_${PN} += "ca-certificates shared-mime-info ttf-bitstream-vera"
RCONFLICTS_${PN} += "wpewebkit"
