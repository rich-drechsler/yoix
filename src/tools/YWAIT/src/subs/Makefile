#
# Makefiles that use common.mk must define variables, like ROOT, ALLFILES,
# LOCAL_ALL, LOCAL_INSTALL, LOCAL_CLEAN and LOCAL_CLOBBER, that appear in
# targets, prerequisites, or variable definitions in rules.mk before they
# include common.mk. Other variables, like INSTALLDIR, that are only used
# in command blocks in rules.mk can be defined after common.mk is included
# and can use definitions from variables.mk.
#

ROOT = ..

SOURCE = \
	ywait_access.pl \
	ywait_changepassword.pl \
	ywait_checkid.pl \
	ywait_cleanuser.pl \
	ywait_getfile.pl \
	ywait_getmessages.pl \
	ywait_isavailable.pl \
	ywait_logout.pl \
	ywait_makeid.pl \
	ywait_makepid.pl \
	ywait_runquery.pl \
	ywait_senddebug.pl \
	ywait_sendmessage._PL \
	ywait_serverdemo.pl \
	ywait_usercommand._PL \
	ywait_userfile.pl \
	ywait_validation.pl \
	ywait_welcome.pl

ALLFILES = $(SOURCE:._PL=.pl)

include $(ROOT)/make/common.mk

INSTALLDIR = $(SERVER_HOMEDIR)/subs

