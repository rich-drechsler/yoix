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
	ywait_byzgraf_example.yx \
	ywait_byzgraf_plot.yx \
	ywait_dndlist_example.yx \
	ywait_edit_example.yx \
	ywait_selector_example1.yx \
	ywait_server_example1.yx \
	ywait_server_example2.yx \
	ywait_server_example3.yx \
	ywait_table_example1.yx \
	ywait_table_example2.yx \
	ywait_tree_example1.yx

ALLFILES = $(SOURCE)

include $(ROOT)/make/common.mk

INSTALLDIR = $(SERVER_HOMEDIR)/screens

