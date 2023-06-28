import bpy
import pandas as pd
import csv
import bmesh

df = pd.read_csv('/home/arzt/Datasets/Mette/E1.csv', engine='python', sep=",\s+")

def add_mesh(name, verts, edges, faces,  collection_name="Collection"):    
    mesh = bpy.data.meshes.new("mesh")
    mesh.from_pydata(verts, edges, faces)
    obj = bpy.data.objects.new(mesh.name, mesh)
    bring_object_to_scene(obj, collection_name)
    return mesh


def bring_object_to_scene(object, collection_name="Collection"):
    col = bpy.data.collections[collection_name]
    col.objects.link(object)
    bpy.context.view_layer.objects.active = object


df_xyz = df.filter(items=["x","y","z"])
verts = list(df_xyz.itertuples(index=False, name=None))
df_edges = df['parent_id'].dropna().astype(int)
edges = list(zip(df_edges.index, df_edges))
faces = [[0,1,2]]
mesh = add_mesh("mesh", verts, edges, faces)


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


mesh.attributes.new('timepoint', 'FLOAT', 'POINT')
d = mesh.attributes['timepoint'].data
for index, t in df['timepoint'].items():
    d[index].value = t

mesh.attributes.new('color_vector', 'FLOAT_VECTOR', 'POINT')
d2 = mesh.attributes['color_vector'].data
for index, color_str in df['2d112_many_colors'].items():
    d2[index].vector = convert_color(colorStringToInt(str(color_str)))

