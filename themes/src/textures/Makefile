TEXTURES = \
	texture_metal.ktx \
	texture_bricks.ktx \
	texture_carpet_blue.ktx \
	texture_grass.ktx \
	texture_table_1.ktx \
	texture_table_2.ktx \
	texture_velvet.ktx \
	texture_wood_fine.ktx \
	field_wood.ktx \
	field_marble.ktx

%.ktx: %.png
	@# xhdpi height: 2 * 64dp
	mkdir -p xhdpi hdpi
	convert $*.png -crop 512x128+0+0 xhdpi/$*_preview.png
	convert $*.png -crop 512x128+0+0 -scale 384x96 hdpi/$*_preview.png
	etcpack $*.png . -ktx -c etc1 -mipmaps -s slow > /dev/null

all: textures

textures: $(TEXTURES)

clean:
	rm -f $(TEXTURES) xhdpi/*_preview.png hdpi/*_preview.png

install: all
	cp $(TEXTURES) ../main/assets/textures/
	cp hdpi/*_preview.png ../main/res/drawable-hdpi/
	cp xhdpi/*_preview.png ../main/res/drawable-xhdpi/
