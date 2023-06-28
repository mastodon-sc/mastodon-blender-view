import bpy
import pandas as pd
import csv
import bmesh
from collections import defaultdict


def read_csv(filename):
    dtype = defaultdict(lambda: str, {'id':int, 'label':str, 'timepoint': int, 'x': float, 'y': float, 'z': float, 'parent_id': float})
    return pd.read_csv(filename, sep=",", skipinitialspace=True, quoting=csv.QUOTE_ALL, dtype=dtype)


def add_mesh(name, vertices, edges=[], faces=[],  collection_name="Collection"):    
    mesh = bpy.data.meshes.new("mesh")
    mesh.from_pydata(vertices, edges, faces)
    obj = bpy.data.objects.new(mesh.name, mesh)
    bring_object_to_scene(obj, collection_name)
    return mesh


def bring_object_to_scene(object, collection_name="Collection"):
    col = bpy.data.collections[collection_name]
    col.objects.link(object) 
    bpy.context.view_layer.objects.active = object


def extract_vertices(df):
    df_xyz = df.filter(items=["x","y","z"])
    return list(df_xyz.itertuples(index=False, name=None))


filename = '/home/arzt/Datasets/Mette/E1.csv'
df = read_csv(filename)
df_edges = df.dropna(subset='parent_id').loc[:,['id','parent_id','timepoint']].reset_index()
edges = list(df_edges.filter(items=["id","parent_id"]).astype(int).itertuples(index=False, name=None))
mesh = add_mesh("mesh", vertices=extract_vertices(df), edges=edges)


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

mesh.attributes.new('target_timepoint', 'FLOAT', 'EDGE')
d = mesh.attributes['target_timepoint'].data
for index, t in df_edges['timepoint'].items():
    d[index].value = t