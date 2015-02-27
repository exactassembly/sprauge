require 'rake'
#require 'git'
require 'logger'
begin
    require 'rummager'
    rescue LoadError
    puts "You must install the rummager gem!"
    puts "    gem install --user-install rummager-x.y.z.gem"
    abort
end

PROJ_NAME="sprauge"

Rummager.repo_base = "#{PROJ_NAME}_#{Etc.getlogin}"
CNTNR_USER="minion"

PROJ_POSTFIX="#{PROJ_NAME}"
CNTNR_BUILD_FILES = 'bldfiles' + PROJ_POSTFIX
CNTNR_DEVENV = 'devenv' + PROJ_POSTFIX

#APT_PROXY="10.1.10.2:3142"
APT_PROXY="192.168.3.254:3142"

GIT_NAME="Ted Vaida"
GIT_EMAIL="ted@kstechnologies.com"

FILENAME_BSP='edison-src-rel1-maint-rel1-ww42-14.tgz'
MIRROR_INTEL='http://downloadmirror.intel.com/24389/eng'

HOST_EXTSRC_PATH="/sprauge"
CNTNR_EXTSRC_PATH="/extsrc"
HOST_DLCACHE_PATH="/download-cache"
CNTNR_DLCACHE_PATH="/downloads"

URL_BSP_SOURCE="#{MIRROR_INTEL}/#{FILENAME_BSP}"
DIR_BUILD_FILES="/build"

EXEC_GIT_EMAIL={
    :cmd => ["git","config","--global","user.email","#{GIT_EMAIL}"],
}

EXEC_GIT_NAME={
    :cmd => ["git","config","--global","user.name","#{GIT_NAME}"],
}

EXEC_CHOWN_SOURCES={
    :cmd => ["/usr/bin/sudo","/bin/chown","--","#{CNTNR_USER}",
    "#{DIR_BUILD_FILES}"],
}

EXEC_FETCH_SOURCES={
    :cmd => ["/bin/sh","-c","curl -s #{URL_BSP_SOURCE} | tar zx -C #{DIR_BUILD_FILES}"],
}

EXEC_SETUP_SOURCES={
    :cmd => ["/bin/bash","-c","#{DIR_BUILD_FILES}/edison-src/device-software/setup.sh --build_name='#{PROJ_NAME}'"],
}

DIR_BUILD="/build/edison-src/build"
DIR_BBCONF="#{DIR_BUILD}/conf"
FILE_CONF_LOCAL="#{DIR_BBCONF}/local.conf"
FILE_CONF_BBLAYERS="#{DIR_BBCONF}/bblayers.conf"

YOCTO_CONF_APPEND= <<EOM
SOURCE_MIRROR_URL ?= "file://#{CNTNR_DLCACHE_PATH}"
INHERIT += "own-mirrors"
BB_GENERATE_MIRROR_TARBALLS = "1"
EOM

EXEC_APPEND_CONF={
:cmd => ["/bin/sh","-c","echo '#{YOCTO_CONF_APPEND}' >> #{FILE_CONF_LOCAL}"]
}

SED_BBLAYERS_CMD1= <<EOM
  \%/build/edison-src/device-software/meta-edison-devtools% i\
  /extsrc/src/meta-molecule/ \\
EOM

EXEC_SED_BBLAYERS={ :cmd => ["/bin/sed","-e","#{SED_BBLAYERS_CMD1}","-i",FILE_CONF_BBLAYERS] },

PROFILE_APPEND= <<EOM
  source /build/edison-src/poky/oe-init-build-env #{DIR_BUILD}
EOM

EXEC_APPEND_PROFILE={
    :cmd => ["/bin/sh","-c","echo '#{PROFILE_APPEND}' >> ~/.profile"]
}

# Build files (generated files)
Rummager::ClickImage.new 'img_buildfiles', {
    :source => %Q{
        FROM busybox
        VOLUME /build
        CMD ["/bin/false"]
    },
    :noclean => true,
}

# Upstream sources files (unpacked Yocto & Digi)
Rummager::ClickImage.new 'img_sources', {
    :source => %Q{
        FROM busybox
        VOLUME /sources
        CMD ["/bin/false"]
    },
    :noclean => true,
}

Rummager::ClickImage.new 'img_wheezy', {
    :noclean => true,
    :source => %Q{
        FROM debian:wheezy
        
        RUN DEBIAN_FRONTEND=noninteractive apt-get update && \
        DEBIAN_FRONTEND=noninteractive apt-get upgrade -y && \
        DEBIAN_FRONTEND=noninteractive apt-get install -y netcat
        
        # Configure timezone and locale
        RUN echo "UTC" > /etc/timezone && \
        dpkg-reconfigure -f noninteractive tzdata
        
        ADD scripts/detect-http-proxy /etc/apt/detect-http-proxy
        RUN chmod +x /etc/apt/detect-http-proxy
        ADD scripts/30detectproxy /etc/apt/apt.conf.d/30detectproxy
        
        ENV APT_PROXY #{APT_PROXY}
        ENTRYPOINT ["/bin/bash","--login"]
        CMD ["-s"]
        
    },
    :add_files => [
        'scripts/detect-http-proxy',
        'scripts/30detectproxy',
    ]
}

Rummager::ClickContainer.new CNTNR_BUILD_FILES, {
    :image_name => 'img_buildfiles',
    :noclean => false,
    :start_once => true,
}

Rummager::ClickImage.new 'img_dev', {
    :noclean => true,
    :dep_image => 'img_wheezy',
    :source => %Q{
        FROM #{Rummager.repo_base}/img_wheezy
        
        # Install Core packages
        RUN DEBIAN_FRONTEND=noninteractive apt-get install -y \
        apt-utils sed rsync gawk wget curl unzip sudo cpio chrpath \
        make build-essential gcc-multilib libtool autoconf automake \
        cvs subversion git-core quilt diffstat libssl-dev \
        vim srecord xterm texinfo libsdl1.2-dev x11vnc xvfb \
        twm procps net-tools screen ncurses-dev \
        nano smartpm rpm python-rpm
        
        RUN DEBIAN_FRONTEND=noninteractive apt-get clean
        
        RUN /usr/sbin/useradd minion -m -d /home/minion \
            && echo "minion:minion" | chpasswd \
            && /usr/sbin/adduser minion sudo \
            && echo "%sudo  ALL=(ALL) NOPASSWD:ALL" >> /etc/sudoers
        
        USER minion
        WORKDIR /build
        
        ENTRYPOINT ["/bin/bash","--login"]
        CMD ["-s"]
    }
}

Rummager::ClickContainer.new 'devenv', {
    :image_name => 'img_dev',
    :volumes_from => [CNTNR_BUILD_FILES],
    :binds => [ "#{HOST_EXTSRC_PATH}:#{CNTNR_EXTSRC_PATH}",
    "#{HOST_DLCACHE_PATH}:#{CNTNR_DLCACHE_PATH}" ],
    :exposed_ports => [ "5900/tcp", "6080/tcp", ],
    :publishall => true,
    :allow_enter => true,
    :exec_once => [
      EXEC_GIT_EMAIL,
      EXEC_GIT_NAME,
      EXEC_CHOWN_SOURCES,
      EXEC_FETCH_SOURCES,
      EXEC_SETUP_SOURCES,
      EXEC_SED_BBLAYERS,
      EXEC_APPEND_CONF,
      EXEC_APPEND_PROFILE,
    ],
}

task :default => [ :"containers:devenv:enter" ]
