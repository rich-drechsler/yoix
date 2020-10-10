#
# Makefiles that use common.mk must define variables, like ROOT, ALLFILES,
# LOCAL_ALL, LOCAL_INSTALL, LOCAL_CLEAN and LOCAL_CLOBBER, that appear in
# targets, prerequisites, or variable definitions in rules.mk before they
# include common.mk. Other variables, like INSTALLDIR, that are only used
# in command blocks in rules.mk can be defined after common.mk is included
# and can use definitions from variables.mk.
#

ROOT = ..

HTMLFILES = index._HTML

YOIXFILES = \
	ywait._YX \
	ywait_addon.yx \
	ywait_broadcast.yx \
	ywait_byzgraf.yx \
	ywait_common.yx \
	ywait_confirm.yx \
	ywait_constructors.yx \
	ywait_csp._YX \
	ywait_custom._YX \
	ywait_debug.yx \
	ywait_dndlist_constructors.yx \
	ywait_edit_constructors.yx \
	ywait_edit_support.yx \
	ywait_edit_templates.yx \
	ywait_icons.yx \
	ywait_insets.yx \
	ywait_login_password.yx \
	ywait_login.yx \
	ywait_main.yx \
	ywait_notify.yx \
	ywait_plugins.yx \
	ywait_preferences.yx \
	ywait_reconnect.yx \
	ywait_reconnect_password.yx \
	ywait_screens._YX \
	ywait_server._YX \
	ywait_threads.yx \
	ywait_welcome.yx

SOURCE = $(YOIXFILES) $(HTMLFILES)

ALLFILES = $(YOIXFILES:._YX=.yx) $(HTMLFILES:._HTML=.html)

include $(ROOT)/make/common.mk

INSTALLDIR = $(SERVER_DOCDIR)

