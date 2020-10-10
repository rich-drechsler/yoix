#
# Java3D module support for Yoix
#

ROOT = ../../../..
MAKEFILE = $(firstword $(MAKEFILE_LIST))

include $(ROOT)/make/common.mk

CLASSPATH = $(ROOT)
YOIXMAIN = att.research.yoix.YoixMain

SOURCE = \
	BodyAlpha.java \
	BodyAmbientLight.java \
	BodyAppearance.java \
	BodyBounds.java \
	BodyBranchGroup.java \
	BodyColorCube.java \
	BodyColoringAttributes.java \
	BodyCone.java \
	BodyCylinder.java \
	BodyDirectionalLight.java \
	BodyGeometryArray.java \
	BodyGeometryStripArray.java \
	BodyGroup.java \
	BodyInterpolator.java \
	BodyLight.java \
	BodyLineArray.java \
	BodyLineAttributes.java \
	BodyLineStripArray.java \
	BodyLocale3D.java \
	BodyMaterial.java \
	BodyNode.java \
	BodyNodeComponent.java \
	BodyPointArray.java \
	BodyPointAttributes.java \
	BodyPointLight.java \
	BodyPolygonAttributes.java \
	BodyPrimitive.java \
	BodyQuadArray.java \
	BodyRenderingAttributes.java \
	BodySceneGraphObject.java \
	BodySceneLoader.java \
	BodyShape3D.java \
	BodySphere.java \
	BodyText2D.java \
	BodyTexture.java \
	BodyTexture2D.java \
	BodyTextureUnit.java \
	BodyTransform3D.java \
	BodyTransparencyAttributes.java \
	BodyTriangleArray.java \
	BodyTriangleFanArray.java \
	BodyTriangleStripArray.java \
	BodyViewPlatform.java \
	BodyVirtualUniverse.java \
	Constants.java \
	ConstantsErrorName.java \
	J3DAlpha.java \
	J3DCanvas3D.java \
	J3DConstants.java \
	J3DInterpolator.java \
	J3DMake.java \
	J3DObject.java \
	J3DPointerActive.java \
	Make.java \
	MiscSceneGraphObject.java \
	MiscTag.java \
	Module.java

all : $(SOURCE:.java=.class)

j3d.jar : all
	@rm -f $@
	cd $(ROOT); $(JAR) cvf att/research/yoix/j3d/$@ att/research/yoix/j3d/*.class

yoix_j3d.jar : all
	@rm -f $@
	cd ..; $(MAKE) -f yoix.mk
	cd $(ROOT); $(JAR) cvfm att/research/yoix/j3d/$@ att/research/yoix/yoix.mf att/research/yoix/*.class att/research/yoix/j3d/*.class

run : all
	CLASSPATH=$(CLASSPATH) $(JAVA) $(JAVAFLAGS) $(YOIXMAIN) $(RUNARGS)

