SUMMARY = "Bluetooth HCI Library"
SECTION = "libs"
AUTHOR = "sandeepmistry"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=461a4d93dce2e9cff358d977ec302364"

DEPENDS = "nodejs nodejs-native"

SRC_URI = "git://github.com/sandeepmistry/noble.git;protocol=https"
SRCREV = "d98b2b1b96adca0d4187cd4a18291ef169ff3e18"

S = "${WORKDIR}/git"

do_compile () {
    # changing the home directory to the working directory, the .npmrc will be created in this directory
    export HOME=${WORKDIR}

    # does not build dev packages
    npm config set dev false

    # access npm registry using http
    npm set strict-ssl false
    npm config set registry http://registry.npmjs.org/

    # configure http proxy if neccessary
    if [ -n "${http_proxy}" ]; then
    npm config set proxy ${http_proxy}
    fi
    if [ -n "${HTTP_PROXY}" ]; then
    npm config set proxy ${HTTP_PROXY}
    fi

    # configure cache to be in working directory
    npm set cache ${WORKDIR}/npm_cache

    # clear local cache prior to each compile
    npm cache clear

    # compile and install  node modules in source directory
    npm --arch=${TARGET_ARCH} --production --verbose install
}

do_install () {
    #force npm to install modules to correct place
    export npm_config_prefix=${D}${prefix}
    export TMPDIR=${T}
    #install from fetched files
    npm -g install ${S} --no-registry --cache ${WORKDIR}/npm_cache
}