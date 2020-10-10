#
# JVM assembler support makefile
#

ROOT = ../../../..
MAKEFILE = $(firstword $(MAKEFILE_LIST))

include $(ROOT)/make/common.mk

SOURCE = \
	JVMAssembler.java \
	JVMAssemblerError.java \
	JVMAttribute.java \
	JVMAttributesTable.java \
	JVMClassFile.java \
	JVMClassLoader.java \
	JVMCode.java \
	JVMConstantPool.java \
	JVMConstantValue.java \
	JVMConstants.java \
	JVMDeprecated.java \
	JVMDescriptor.java \
	JVMDisassembler.java \
	JVMExceptions.java \
	JVMField.java \
	JVMFieldsTable.java \
	JVMInnerClasses.java \
	JVMInstruction.java \
	JVMInterfacesTable.java \
	JVMLineNumberTable.java \
	JVMLocalVariableTable.java \
	JVMMethod.java \
	JVMMethodsTable.java \
	JVMMisc.java \
	JVMOpcodes.java \
	JVMPatterns.java \
	JVMScanner.java \
	JVMSourceFile.java \
	JVMSynthetic.java \
	JVMTypeStack.java \
	JVMUnimplemented.java

all : $(SOURCE:.java=.class)
	@:

clean :
	rm -f *.class

clobber : clean
	@:

install run :
	@:

