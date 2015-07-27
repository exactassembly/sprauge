# sprauge

Yocto automation toolkit for Intel Edison

# Prerequisites

* Docker - http://www.docker.io
* Rummager (Ruby Rake library) - https://github.com/exactassembly/rummager

# How to build
Use the supplied Gemfile to configure your Ruby environment, run:

    bundler

Then you can run rake:

    bundler exec rake

This will bring you into a pre-built Docker container based on Debian, with the Yocto tools, and Intel Edison source pre-installed and configured.

The typical build with Yocto is:

    bitbake core-image-minmal

# Customization

Follow standard Yocto methodology:  http://www.yoctoproject.org