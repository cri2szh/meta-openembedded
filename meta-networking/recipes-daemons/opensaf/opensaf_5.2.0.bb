SUMMARY = "OpenSAF is an open source implementation of the SAF AIS specification"
DESCRIPTION = "OpenSAF is an open source project established to develop a base platform \
middleware consistent with Service Availability Forum (SA Forum) \
specifications, under the LGPLv2.1 license. The OpenSAF Foundation was \
established by leading Communications and Enterprise Computing Companies to \
facilitate the OpenSAF Project and to accelerate the adoption of the OpenSAF \
code base in commercial products. \
The OpenSAF project was launched in mid 2007 and has been under development by \
an informal group of supporters of the OpenSAF initiative. The OpenSAF \
Foundation was founded on January 22nd 2008 with Emerson Network Power, \
Ericsson, Nokia Siemens Networks, HP and Sun Microsystems as founding members."
HOMEPAGE = "http://www.opensaf.org"
SECTION = "admin"
LICENSE = "LGPLv2.1"
LIC_FILES_CHKSUM = "file://COPYING.LIB;md5=a916467b91076e631dd8edb7424769c7"

DEPENDS = "libxml2 python"
TOOLCHAIN = "gcc"

SECURITY_CFLAGS = "${SECURITY_NO_PIE_CFLAGS}"

SRC_URI = "${SOURCEFORGE_MIRROR}/${BPN}/releases/${BPN}-${PV}.tar.gz \
           file://0001-configure-Pass-linker-specific-options-with-Wl.patch \
           file://0001-configure-Disable-format-overflow-if-supported-by-gc.patch \
           file://0001-Remove-unused-variables.patch \
"
SRC_URI[md5sum] = "08991fd467ae9dcea3c8747be8e3981e"
SRC_URI[sha256sum] = "903478244afe37e329be93050f1d48fa18c84ea17862134c4217b920e267a04a"

inherit autotools useradd systemd pkgconfig

USERADD_PACKAGES = "${PN}"
GROUPADD_PARAM_${PN} = "-f -r opensaf"
USERADD_PARAM_${PN} =  "-r -g opensaf -d ${datadir}/opensaf/ -s ${sbindir}/nologin -c \"OpenSAF\" opensaf"

SYSTEMD_SERVICE_${PN} += "opensafd.service"
SYSTEMD_AUTO_ENABLE = "disable"

PACKAGECONFIG[systemd] = ",,systemd"
PACKAGECONFIG[openhpi] = "--with-hpi-interface=B03 --enable-ais-plm,,openhpi"

PACKAGECONFIG_append = "${@bb.utils.contains('DISTRO_FEATURES', 'systemd', ' systemd', '', d)}"

PKGLIBDIR="${libdir}"

LDFLAGS += "-Wl,--as-needed -latomic -Wl,--no-as-needed"

do_install_append() {
    cp -av --no-preserve=ownership ${B}/lib/.libs/*.so* ${D}${libdir}
    rm -fr "${D}${localstatedir}/lock"
    rm -fr "${D}${localstatedir}/run"
    rmdir --ignore-fail-on-non-empty "${D}${localstatedir}"
    rmdir --ignore-fail-on-non-empty "${D}${datadir}/java"
    if [ ! -d "${D}${sysconfdir}/init.d" ]; then
        install -d ${D}${sysconfdir}/init.d
        install -m 0755 ${B}/osaf/services/infrastructure/nid/scripts/opensafd ${D}${sysconfdir}/init.d/
    fi
}

FILES_${PN} += "${systemd_unitdir}/system/*.service"
FILES_${PN}-staticdev += "${PKGLIBDIR}/*.a"

INSANE_SKIP_${PN} = "dev-so"

RDEPENDS_${PN} += "bash python"

