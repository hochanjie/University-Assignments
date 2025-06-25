from PIL import Image
im = Image.open('retromushroom.png', 'r')
pix = im.load()
print(im.size)
print(pix)
print(pix[0,0])
width, height = im.size[0], im.size[1]

def rgb_to_hex(rgb):
    r = int(rgb[0])
    g = int(rgb[1])
    b = int(rgb[2])
    newrgb = (r, g, b)
    return '#%02x%02x%02x' % newrgb


col_array = []
for row in range(width):
    row_array = []
    for col in range(height):

        row_array.append(rgb_to_hex(pix[col, row]))
    col_array.append(row_array)

print(col_array)

