import bpy
import pandas as pd
import csv
import bmesh

df = pd.read_csv('/home/arzt/Datasets/Mette/E1.csv', engine='python', sep=",\s+")

def add_mesh(name, verts, faces, edges=None, col_name="Collection"):    
    if edges is None:
        edges = []
    mesh = bpy.data.meshes.new(name)
    obj = bpy.data.objects.new(mesh.name, mesh)
    col = bpy.data.collections[col_name]
    col.objects.link(obj)
    bpy.context.view_layer.objects.active = obj
    mesh.from_pydata(verts, edges, faces)
    return mesh

df_xyz = df.filter(items=["x","y","z"])
verts = list(df_xyz.itertuples(index=False, name=None))
faces = [[0, 1, 2]]

mesh = add_mesh("mesh", verts, faces)


mesh.attributes.new('timepoint', 'FLOAT', 'POINT')
d = mesh.attributes['timepoint'].data
for index, t in df['timepoint'].items():
    d[index].value = t
    

def colorStringToInt(s):
    default_color = 0xcccccc
    try:
        return int(s[1:], 16)
    except ValueError:
        return default_color    

def convert_color(color_code):
    red = ((color_code >> 16) & 0xFF) / 255.0
    green = ((color_code >> 8) & 0xFF) / 255.0
    blue = (color_code & 0xFF) / 255.0
    return (red, green, blue)

mesh.attributes.new('color', 'INT', 'POINT')
d = mesh.attributes['color'].data

mesh.attributes.new('color_vector', 'FLOAT_VECTOR', 'POINT')
d2 = mesh.attributes['color_vector'].data

for index, color_str in df['2d112_many_colors'].items():
    color_code = colorStringToInt(str(color_str))
    d[index].value = color_code
    d2[index].vector = convert_color(color_code)

