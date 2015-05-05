require 'rubygems'
require 'bundler/setup'
Bundler.require(:default)

PROJ_NAME="sprauge"

if FileTest::exists?(File.join( Rake.application.original_dir, 'local.conf') )
    puts "using local.conf" if Rake.verbose == true
    load File.join( Rake.application.original_dir, 'local.conf')
elsif FileTest::exists?(File.join( Rake.application.original_dir, 'local.conf.rb') )
    puts "using local.conf" if Rake.verbose == true
    load File.join( Rake.application.original_dir, 'local.conf.rb')
else
    raise LoadError, "missing local.conf! please copy from local.conf.example"
end

if defined? LOCAL_REPO
    Rummager.repo_base = LOCAL_REPO
else
    Rummager.repo_base = "#{PROJ_NAME}_#{Etc.getlogin}"
end

CNTNR_USER="minion"

PROJ_POSTFIX="#{PROJ_NAME}"
CNTNR_BUILD_FILES = 'bldfiles' + PROJ_POSTFIX
CNTNR_DEVENV = 'devenv' + PROJ_POSTFIX

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
    :cmd => ["/bin/sh","-c","echo '#{YOCTO_CONF_APPEND}' >> #{FILE_CONF_LOCAL}"],
}

SED_BBLAYERS_CMD1= <<EOM
\\%/build/edison-src/device-software/meta-edison-devtools% a\\
  /extsrc/src/meta-sprauge/ \\\\
EOM

EXEC_SED_BBLAYERS={
    :cmd => ["/bin/sed","-e","#{SED_BBLAYERS_CMD1}","-i",FILE_CONF_BBLAYERS],
}

PROFILE_APPEND= <<EOM
  source /build/edison-src/poky/oe-init-build-env #{DIR_BUILD}
EOM

EXEC_APPEND_PROFILE={
    :cmd => ["/bin/sh","-c","echo '#{PROFILE_APPEND}' >> ~/.profile"],
    :restart_after => true,
}

Rummager::ClickCntnrExec.new "add_yocto", {
  :container_name => CNTNR_DEVENV,
  :exec_list => [
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


Rummager::ClickContainer.new CNTNR_DEVENV, {
    :image_name => 'debian4yocto',
    :image_nobuild => true,
    :repo_base => 'y3ddet',
    :binds => [ "#{HOST_EXTSRC_PATH}:#{CNTNR_EXTSRC_PATH}",
                "#{HOST_DLCACHE_PATH}:#{CNTNR_DLCACHE_PATH}" ],
    :publishall => true,
    :allow_enter => true,
    :enter_dep_jobs => [
        :"add_yocto",
    ]
}

task :default => [ :"containers:#{CNTNR_DEVENV}:enter" ]
