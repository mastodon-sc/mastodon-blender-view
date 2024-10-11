###
# #%L
# A Mastodon plugin data allows to show the embryo in Blender.
# %%
# Copyright (C) 2022 - 2024 Matthias Arzt
# %%
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
# 
# 1. Redistributions of source code must retain the above copyright notice,
#    this list of conditions and the following disclaimer.
# 2. Redistributions in binary form must reproduce the above copyright notice,
#    this list of conditions and the following disclaimer in the documentation
#    and/or other materials provided with the distribution.
# 
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
# ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
# LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
# CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
# SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
# INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
# CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
# ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
# POSSIBILITY OF SUCH DAMAGE.
# #L%
###
import os
import bpy
import pandas as pd
import csv
import bmesh
from collections import defaultdict


filename = os.environ['MASTODON_BLENDER_CSV_FILE']
tag_set = os.environ['MASTODON_BLENDER_TAG_SET']


def read_csv(filename):
    dtype = defaultdict(lambda: str, {'id':int, 'label':str, 'timepoint': int, 'x': float, 'y': float, 'z': float, 'radius': float, 'parent_id': float})
    return pd.read_csv(filename, sep=",", skipinitialspace=True, quoting=csv.QUOTE_ALL, dtype=dtype)


def add_mesh(name, vertices, edges=[], faces=[],  collection_name="Collection"):
    mesh = bpy.data.meshes.new("mesh")
    mesh.from_pydata(vertices, edges, faces)
    obj = bpy.data.objects.new(mesh.name, mesh)
    bring_object_to_scene(obj, collection_name)
    obj.modifiers.new(name='geometry nodes modifier', type='NODES')
    obj.modifiers['geometry nodes modifier'].node_group = bpy.data.node_groups['show spheres and tracks']
    return mesh


def bring_object_to_scene(object, collection_name="Collection"):
    col = bpy.data.collections[collection_name]
    col.objects.link(object)
    bpy.context.view_layer.objects.active = object


def extract_vertices(df):
    df_xyz = df.filter(items=["x","y","z"])
    return list(df_xyz.itertuples(index=False, name=None))


def normalize(df):
    xyz = df[['x','y','z']]
    radius = df['radius']
    xyz = xyz - xyz.mean()
    variance = (xyz**2).sum().sum() / len(xyz)
    factor = 1 / ( variance ** 0.5 )
    xyz = xyz * factor
    radius = radius * factor
    df[['x','y','z']] = xyz
    df['radius'] = radius


#filename = '/home/arzt/Datasets/Mette/E1.csv'
df = read_csv(filename)
normalize(df)
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

mesh.attributes.new('radius', 'FLOAT', 'POINT')
d = mesh.attributes['radius'].data
for index, r in df['radius'].items():
    d[index].value = r

mesh.attributes.new('target_timepoint', 'FLOAT', 'EDGE')
d = mesh.attributes['target_timepoint'].data
for index, t in df_edges['timepoint'].items():
    d[index].value = t

if tag_set in df.columns:
    mesh.attributes.new('color_vector', 'FLOAT_VECTOR', 'POINT')
    d2 = mesh.attributes['color_vector'].data
    for index, color_str in df[tag_set].items():
        d2[index].vector = convert_color(colorStringToInt(str(color_str)))


