TEXTURES = \
	texture_metal.ktx \
	texture_bricks.ktx \
	texture_carpet_blue.ktx \
	texture_grass.ktx \
	texture_table_1.ktx \
	texture_table_2.ktx \
	texture_velvet.ktx \
	texture_wood_fine.ktx


%.ktx: %.png Makefile
	etcpack $*.png . -ktx -c etc1 -mipmaps > /dev/null

all: textures

textures: $(TEXTURES)

clean:
	rm -f $(TEXTURES)

install: all
	cp $(TEXTURES) ../../res/raw/
