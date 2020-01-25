package com.vittach.jumpjack.mesh;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MeshCompress {

    public static Mesh mergeMeshes(
        List<MeshObj> meshObjs,
        Map<String, List<TextureRegion>> textureRegions,
        List<Matrix4> translates
    ) {
        if (meshObjs == null || meshObjs.isEmpty()) return null;

        final List<Float> vertexList = new ArrayList<Float>();
        final List<Short> indiceList = new ArrayList<Short>();

        int destOffset = 0;
        int indexOffset = 0;
        int vertexOffset = 0;

        for (int i = 0; i < meshObjs.size(); i++) {
            Mesh mesh = meshObjs.get(i).getMesh();
            VertexAttribute positionCoordinates = mesh.getVertexAttribute(VertexAttributes.Usage.Position);
            int dimension = positionCoordinates.numComponents;
            int offset = positionCoordinates.offset / 4;

            int numIndices = mesh.getNumIndices();
            int numberVertices = mesh.getNumVertices();
            int vertexSize = mesh.getVertexSize() / 4;
            int length = numberVertices * vertexSize;

            float[] vertices = new float[length];
            mesh.getVertices(0, length, vertices);

            Mesh.transform(translates.get(i), vertices, vertexSize, offset, dimension, 0, numberVertices);

            // Experimental culling invisible surface
            int oneSideVertices = 4 * vertexSize;
            for (int j = 0, k = 0; j < length; j++) {
                boolean isNeedVertices = false;
                if (j >= 2 * oneSideVertices && j < 3 * oneSideVertices) {
                    // bottom surface
                    isNeedVertices = true;
                } else if (j >= 3 * oneSideVertices && j < 4 * oneSideVertices) {
                    // top surface
                    isNeedVertices = true;
                }

                if (isNeedVertices) {
                    vertexList.add(destOffset + k, vertices[j]);
                    k++;
                } else if (j % vertexSize == 0) {
                    numberVertices--;
                }
            }

            short[] indices = new short[numIndices];
            mesh.getIndices(indices);
            int oneSideIndices = numIndices / 6;
            length = numIndices;
            for (int j = 0, k = 0; j < length; j++) {
                boolean isNeedIndices = false;
                if (j >= 0 && j < oneSideIndices) {
                    isNeedIndices = true;
                } else if (j >= oneSideIndices && j < 2 * oneSideIndices) {
                    isNeedIndices = true;
                }

                if (isNeedIndices) {
                    indices[j] += vertexOffset;
                    indiceList.add(indexOffset + k, indices[j]);
                    k++;
                } else {
                    numIndices--;
                }
            }

            reInitTextures(mesh, textureRegions.get(meshObjs.get(i).getSymbol()));

            destOffset += numberVertices * vertexSize;
            vertexOffset += numberVertices;
            indexOffset += numIndices;
        }

        VertexAttributes vertexAttributes = meshObjs.get(0).getMesh().getVertexAttributes();
        Mesh mesh = new Mesh(true, vertexList.size(), indiceList.size(), vertexAttributes);

        float vertices[] = new float[vertexList.size()];
        for (int i = 0; i < vertexList.size(); i++) {
            vertices[i] = vertexList.get(i);
        }
        mesh.setVertices(vertices);

        short indices[] = new short[indiceList.size()];
        for (int i = 0; i < indiceList.size(); i++) {
            indices[i] = indiceList.get(i);
        }
        mesh.setIndices(indices);

        return mesh;
    }

    static void reInitTextures(Mesh mergeMesh, List<TextureRegion> regions) {
        int texture = mergeMesh.getVertexAttributes().getOffset(VertexAttributes.Usage.TextureCoordinates);
        int vertexSize = mergeMesh.getVertexSize() / 4; // divide to convert bytes to floats
        int length = vertexSize * mergeMesh.getNumVertices();
        float[] vertices = new float[length];

        mergeMesh.getVertices(vertices);
        for (int i = texture, corner = 0, side = 0; i < length; i += vertexSize) {
            if (corner == 0) {
                vertices[i] = regions.get(side).getU();
                vertices[i + 1] = regions.get(side).getV();
            } else if (corner == 1) {
                vertices[i] = regions.get(side).getU();
                vertices[i + 1] = regions.get(side).getV2();
            } else if (corner == 2) {
                vertices[i] = regions.get(side).getU2();
                vertices[i + 1] = regions.get(side).getV2();
            } else if (corner == 3) {
                vertices[i] = regions.get(side).getU2();
                vertices[i + 1] = regions.get(side).getV();
                corner = 0;
                side++;
                if (side >= regions.size()) {
                    side = 0;
                }
                continue;
            }
            corner++;
        }

        mergeMesh.setVertices(vertices);
    }
}
