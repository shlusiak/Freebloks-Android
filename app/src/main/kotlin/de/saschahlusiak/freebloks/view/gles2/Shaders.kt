package de.saschahlusiak.freebloks.view.gles2

/**
 * One directional light in eye space, Gouraud (per-vertex) lighting with a material that has the same ambient and
 * diffuse colour, non-local viewer specular, normalized normals, and texturing modulated with the lit colour through a
 * texture matrix.
 *
 * Lighting:
 *   color = matAmb * (globalAmbient + lightAmb) + matDiff * lightDiff * max(N.L, 0)
 *           + (N.L > 0 ? matSpec * lightSpec * pow(max(N.H, 0), shininess) : 0), alpha = matDiff.a
 * with H = normalize(L + (0, 0, 1)) for a non-local viewer, then clamped to [0, 1].
 */
object Shaders {
    const val VERTEX = """
        uniform mat4 uMVP;
        uniform mat3 uNormalMatrix;  // inverse transpose of the upper 3x3 of the model view matrix
        uniform mat4 uTexMatrix;

        uniform bool uLighting;
        uniform vec3 uLightDir;      // eye space, pointing towards the light
        uniform vec3 uLightAmbient;
        uniform vec3 uLightDiffuse;
        uniform vec3 uLightSpecular;
        uniform vec3 uGlobalAmbient;

        uniform vec4 uColor;         // ambient and diffuse material; the flat colour when lighting is off
        uniform vec3 uSpecular;
        uniform float uShininess;

        attribute vec4 aPosition;
        attribute vec3 aNormal;
        attribute vec2 aTexCoord;

        varying vec4 vColor;
        varying vec2 vTexCoord;

        void main() {
            gl_Position = uMVP * aPosition;
            vTexCoord = (uTexMatrix * vec4(aTexCoord, 0.0, 1.0)).xy;

            if (!uLighting) {
                vColor = uColor;
                return;
            }

            // normals are not unit length and shadows are scaled non-uniformly
            vec3 n = normalize(uNormalMatrix * aNormal);
            vec3 l = normalize(uLightDir);
            float nDotL = max(dot(n, l), 0.0);

            vec3 rgb = uColor.rgb * (uGlobalAmbient + uLightAmbient)
                     + uColor.rgb * uLightDiffuse * nDotL;

            if (nDotL > 0.0) {
                vec3 h = normalize(l + vec3(0.0, 0.0, 1.0));
                float nDotH = max(dot(n, h), 0.0);
                float f = uShininess > 0.0 ? pow(nDotH, uShininess) : 1.0;
                rgb += uSpecular * uLightSpecular * f;
            }
            vColor = vec4(clamp(rgb, 0.0, 1.0), clamp(uColor.a, 0.0, 1.0));
        }
    """

    const val FRAGMENT = """
        precision mediump float;

        uniform bool uTexture;
        uniform sampler2D uSampler;

        varying vec4 vColor;
        varying vec2 vTexCoord;

        void main() {
            gl_FragColor = uTexture ? vColor * texture2D(uSampler, vTexCoord) : vColor;
        }
    """
}
