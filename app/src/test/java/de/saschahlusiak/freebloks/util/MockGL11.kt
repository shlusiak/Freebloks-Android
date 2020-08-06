package de.saschahlusiak.freebloks.util

import java.nio.Buffer
import java.nio.FloatBuffer
import java.nio.IntBuffer
import javax.microedition.khronos.opengles.GL11

class MockGL11: GL11 {
    override fun glDisableClientState(array: Int) {
        
    }

    override fun glLineWidth(width: Float) {
        
    }

    override fun glMultMatrixx(m: IntArray?, offset: Int) {
        
    }

    override fun glMultMatrixx(m: IntBuffer?) {
        
    }

    override fun glScalex(x: Int, y: Int, z: Int) {
        
    }

    override fun glGetFixedv(pname: Int, params: IntArray?, offset: Int) {
        
    }

    override fun glGetFixedv(pname: Int, params: IntBuffer?) {
        
    }

    override fun glFrustumx(left: Int, right: Int, bottom: Int, top: Int, zNear: Int, zFar: Int) {
        
    }

    override fun glGetTexEnvxv(env: Int, pname: Int, params: IntArray?, offset: Int) {
        
    }

    override fun glGetTexEnvxv(env: Int, pname: Int, params: IntBuffer?) {
        
    }

    override fun glTexParameterfv(target: Int, pname: Int, params: FloatArray?, offset: Int) {
        
    }

    override fun glTexParameterfv(target: Int, pname: Int, params: FloatBuffer?) {
        
    }

    override fun glEnableClientState(array: Int) {
        
    }

    override fun glGetPointerv(pname: Int, params: Array<out Buffer>?) {
        
    }

    override fun glPointSizex(size: Int) {
        
    }

    override fun glStencilFunc(func: Int, ref: Int, mask: Int) {
        
    }

    override fun glFogx(pname: Int, param: Int) {
        
    }

    override fun glLightModelf(pname: Int, param: Float) {
        
    }

    override fun glLineWidthx(width: Int) {
        
    }

    override fun glGetMaterialfv(face: Int, pname: Int, params: FloatArray?, offset: Int) {
        
    }

    override fun glGetMaterialfv(face: Int, pname: Int, params: FloatBuffer?) {
        
    }

    override fun glRotatef(angle: Float, x: Float, y: Float, z: Float) {
        
    }

    override fun glClientActiveTexture(texture: Int) {
        
    }

    override fun glLightModelxv(pname: Int, params: IntArray?, offset: Int) {
        
    }

    override fun glLightModelxv(pname: Int, params: IntBuffer?) {
        
    }

    override fun glColorMask(red: Boolean, green: Boolean, blue: Boolean, alpha: Boolean) {
        
    }

    override fun glPolygonOffset(factor: Float, units: Float) {
        
    }

    override fun glViewport(x: Int, y: Int, width: Int, height: Int) {
        
    }

    override fun glGetBooleanv(pname: Int, params: BooleanArray?, offset: Int) {
        
    }

    override fun glGetBooleanv(pname: Int, params: IntBuffer?) {
        
    }

    override fun glGetBufferParameteriv(target: Int, pname: Int, params: IntArray?, offset: Int) {
        
    }

    override fun glGetBufferParameteriv(target: Int, pname: Int, params: IntBuffer?) {
        
    }

    override fun glGetLightxv(light: Int, pname: Int, params: IntArray?, offset: Int) {
        
    }

    override fun glGetLightxv(light: Int, pname: Int, params: IntBuffer?) {
        
    }

    override fun glTexEnviv(target: Int, pname: Int, params: IntArray?, offset: Int) {
        
    }

    override fun glTexEnviv(target: Int, pname: Int, params: IntBuffer?) {
        
    }

    override fun glOrthox(left: Int, right: Int, bottom: Int, top: Int, zNear: Int, zFar: Int) {
        
    }

    override fun glTexSubImage2D(target: Int, level: Int, xoffset: Int, yoffset: Int, width: Int, height: Int, format: Int, type: Int, pixels: Buffer?) {
        
    }

    override fun glGetTexParameteriv(target: Int, pname: Int, params: IntArray?, offset: Int) {
        
    }

    override fun glGetTexParameteriv(target: Int, pname: Int, params: IntBuffer?) {
        
    }

    override fun glLightfv(light: Int, pname: Int, params: FloatArray?, offset: Int) {
        
    }

    override fun glLightfv(light: Int, pname: Int, params: FloatBuffer?) {
        
    }

    override fun glGenTextures(n: Int, textures: IntArray?, offset: Int) {
        
    }

    override fun glGenTextures(n: Int, textures: IntBuffer?) {
        
    }

    override fun glLoadMatrixf(m: FloatArray?, offset: Int) {
        
    }

    override fun glLoadMatrixf(m: FloatBuffer?) {
        
    }

    override fun glFogf(pname: Int, param: Float) {
        
    }

    override fun glDeleteTextures(n: Int, textures: IntArray?, offset: Int) {
        
    }

    override fun glDeleteTextures(n: Int, textures: IntBuffer?) {
        
    }

    override fun glTexParameteriv(target: Int, pname: Int, params: IntArray?, offset: Int) {
        
    }

    override fun glTexParameteriv(target: Int, pname: Int, params: IntBuffer?) {
        
    }

    override fun glMaterialxv(face: Int, pname: Int, params: IntArray?, offset: Int) {
        
    }

    override fun glMaterialxv(face: Int, pname: Int, params: IntBuffer?) {
        
    }

    override fun glDeleteBuffers(n: Int, buffers: IntArray?, offset: Int) {
        
    }

    override fun glDeleteBuffers(n: Int, buffers: IntBuffer?) {
        
    }

    override fun glAlphaFuncx(func: Int, ref: Int) {
        
    }

    override fun glShadeModel(mode: Int) {
        
    }

    override fun glFrontFace(mode: Int) {
        
    }

    override fun glClearDepthf(depth: Float) {
        
    }

    override fun glBindTexture(target: Int, texture: Int) {
        
    }

    override fun glPixelStorei(pname: Int, param: Int) {
        
    }

    override fun glTranslatef(x: Float, y: Float, z: Float) {
        
    }

    override fun glHint(target: Int, mode: Int) {
        
    }

    override fun glLoadIdentity() {
        
    }

    override fun glMatrixMode(mode: Int) {
        
    }

    override fun glPointParameterfv(pname: Int, params: FloatArray?, offset: Int) {
        
    }

    override fun glPointParameterfv(pname: Int, params: FloatBuffer?) {
        
    }

    override fun glDepthMask(flag: Boolean) {
        
    }

    override fun glMultMatrixf(m: FloatArray?, offset: Int) {
        
    }

    override fun glMultMatrixf(m: FloatBuffer?) {
        
    }

    override fun glBufferData(target: Int, size: Int, data: Buffer?, usage: Int) {
        
    }

    override fun glTexParameterf(target: Int, pname: Int, param: Float) {
        
    }

    override fun glTexEnvi(target: Int, pname: Int, param: Int) {
        
    }

    override fun glTexEnvxv(target: Int, pname: Int, params: IntArray?, offset: Int) {
        
    }

    override fun glTexEnvxv(target: Int, pname: Int, params: IntBuffer?) {
        
    }

    override fun glTranslatex(x: Int, y: Int, z: Int) {
        
    }

    override fun glCompressedTexSubImage2D(target: Int, level: Int, xoffset: Int, yoffset: Int, width: Int, height: Int, format: Int, imageSize: Int, data: Buffer?) {
        
    }

    override fun glBindBuffer(target: Int, buffer: Int) {
        
    }

    override fun glDisable(cap: Int) {
        
    }

    override fun glGetTexParameterxv(target: Int, pname: Int, params: IntArray?, offset: Int) {
        
    }

    override fun glGetTexParameterxv(target: Int, pname: Int, params: IntBuffer?) {
        
    }

    override fun glGetClipPlanef(pname: Int, eqn: FloatArray?, offset: Int) {
        
    }

    override fun glGetClipPlanef(pname: Int, eqn: FloatBuffer?) {
        
    }

    override fun glClearColor(red: Float, green: Float, blue: Float, alpha: Float) {
        
    }

    override fun glScissor(x: Int, y: Int, width: Int, height: Int) {
        
    }

    override fun glTexParameterx(target: Int, pname: Int, param: Int) {
        
    }

    override fun glPushMatrix() {
        
    }

    override fun glGetTexParameterfv(target: Int, pname: Int, params: FloatArray?, offset: Int) {
        
    }

    override fun glGetTexParameterfv(target: Int, pname: Int, params: FloatBuffer?) {
        
    }

    override fun glTexImage2D(target: Int, level: Int, internalformat: Int, width: Int, height: Int, border: Int, format: Int, type: Int, pixels: Buffer?) {
        
    }

    override fun glBlendFunc(sfactor: Int, dfactor: Int) {
        
    }

    override fun glVertexPointer(size: Int, type: Int, stride: Int, offset: Int) {
        
    }

    override fun glVertexPointer(size: Int, type: Int, stride: Int, pointer: Buffer?) {
        
    }

    override fun glFogfv(pname: Int, params: FloatArray?, offset: Int) {
        
    }

    override fun glFogfv(pname: Int, params: FloatBuffer?) {
        
    }

    override fun glIsEnabled(cap: Int): Boolean {
        return false
    }

    override fun glClearDepthx(depth: Int) {
        
    }

    override fun glClipPlanex(plane: Int, equation: IntArray?, offset: Int) {
        
    }

    override fun glClipPlanex(plane: Int, equation: IntBuffer?) {
        
    }

    override fun glLightx(light: Int, pname: Int, param: Int) {
        
    }

    override fun glPointParameterxv(pname: Int, params: IntArray?, offset: Int) {
        
    }

    override fun glPointParameterxv(pname: Int, params: IntBuffer?) {
        
    }

    override fun glDepthRangef(zNear: Float, zFar: Float) {
        
    }

    override fun glFlush() {
        
    }

    override fun glLightxv(light: Int, pname: Int, params: IntArray?, offset: Int) {
        
    }

    override fun glLightxv(light: Int, pname: Int, params: IntBuffer?) {
        
    }

    override fun glSampleCoverage(value: Float, invert: Boolean) {
        
    }

    override fun glColor4f(red: Float, green: Float, blue: Float, alpha: Float) {
        
    }

    override fun glColorPointer(size: Int, type: Int, stride: Int, offset: Int) {
        
    }

    override fun glColorPointer(size: Int, type: Int, stride: Int, pointer: Buffer?) {
        
    }

    override fun glCopyTexSubImage2D(target: Int, level: Int, xoffset: Int, yoffset: Int, x: Int, y: Int, width: Int, height: Int) {
        
    }

    override fun glTexParameterxv(target: Int, pname: Int, params: IntArray?, offset: Int) {
        
    }

    override fun glTexParameterxv(target: Int, pname: Int, params: IntBuffer?) {
        
    }

    override fun glLightf(light: Int, pname: Int, param: Float) {
        
    }

    override fun glPointSizePointerOES(type: Int, stride: Int, pointer: Buffer?) {
        
    }

    override fun glPointSize(size: Float) {
        
    }

    override fun glGetLightfv(light: Int, pname: Int, params: FloatArray?, offset: Int) {
        
    }

    override fun glGetLightfv(light: Int, pname: Int, params: FloatBuffer?) {
        
    }

    override fun glPointParameterx(pname: Int, param: Int) {
        
    }

    override fun glClear(mask: Int) {
        
    }

    override fun glDepthFunc(func: Int) {
        
    }

    override fun glColor4ub(red: Byte, green: Byte, blue: Byte, alpha: Byte) {
        
    }

    override fun glIsBuffer(buffer: Int): Boolean {
        return false
    }

    override fun glNormal3f(nx: Float, ny: Float, nz: Float) {
        
    }

    override fun glGetClipPlanex(pname: Int, eqn: IntArray?, offset: Int) {
        
    }

    override fun glGetClipPlanex(pname: Int, eqn: IntBuffer?) {
        
    }

    override fun glDrawElements(mode: Int, count: Int, type: Int, offset: Int) {
        
    }

    override fun glDrawElements(mode: Int, count: Int, type: Int, indices: Buffer?) {
        
    }

    override fun glTexParameteri(target: Int, pname: Int, param: Int) {
        
    }

    override fun glColor4x(red: Int, green: Int, blue: Int, alpha: Int) {
        
    }

    override fun glGetTexEnviv(env: Int, pname: Int, params: IntArray?, offset: Int) {
        
    }

    override fun glGetTexEnviv(env: Int, pname: Int, params: IntBuffer?) {
        
    }

    override fun glTexEnvx(target: Int, pname: Int, param: Int) {
        
    }

    override fun glFinish() {
        
    }

    override fun glGetIntegerv(pname: Int, params: IntArray?, offset: Int) {
        
    }

    override fun glGetIntegerv(pname: Int, params: IntBuffer?) {
        
    }

    override fun glOrthof(left: Float, right: Float, bottom: Float, top: Float, zNear: Float, zFar: Float) {
        
    }

    override fun glNormal3x(nx: Int, ny: Int, nz: Int) {
        
    }

    override fun glMultiTexCoord4f(target: Int, s: Float, t: Float, r: Float, q: Float) {
        
    }

    override fun glDepthRangex(zNear: Int, zFar: Int) {
        
    }

    override fun glLightModelx(pname: Int, param: Int) {
        
    }

    override fun glActiveTexture(texture: Int) {
        
    }

    override fun glCullFace(mode: Int) {
        
    }

    override fun glClearStencil(s: Int) {
        
    }

    override fun glTexEnvf(target: Int, pname: Int, param: Float) {
        
    }

    override fun glLoadMatrixx(m: IntArray?, offset: Int) {
        
    }

    override fun glLoadMatrixx(m: IntBuffer?) {
        
    }

    override fun glGetFloatv(pname: Int, params: FloatArray?, offset: Int) {
        
    }

    override fun glGetFloatv(pname: Int, params: FloatBuffer?) {
        
    }

    override fun glMaterialf(face: Int, pname: Int, param: Float) {
        
    }

    override fun glDrawArrays(mode: Int, first: Int, count: Int) {
        
    }

    override fun glLightModelfv(pname: Int, params: FloatArray?, offset: Int) {
        
    }

    override fun glLightModelfv(pname: Int, params: FloatBuffer?) {
        
    }

    override fun glGetError(): Int {
        return 0
    }

    override fun glLogicOp(opcode: Int) {
        
    }

    override fun glBufferSubData(target: Int, offset: Int, size: Int, data: Buffer?) {
        
    }

    override fun glClearColorx(red: Int, green: Int, blue: Int, alpha: Int) {
        
    }

    override fun glCopyTexImage2D(target: Int, level: Int, internalformat: Int, x: Int, y: Int, width: Int, height: Int, border: Int) {
        
    }

    override fun glMaterialfv(face: Int, pname: Int, params: FloatArray?, offset: Int) {
        
    }

    override fun glMaterialfv(face: Int, pname: Int, params: FloatBuffer?) {
        
    }

    override fun glScalef(x: Float, y: Float, z: Float) {
        
    }

    override fun glNormalPointer(type: Int, stride: Int, offset: Int) {
        
    }

    override fun glNormalPointer(type: Int, stride: Int, pointer: Buffer?) {
        
    }

    override fun glTexCoordPointer(size: Int, type: Int, stride: Int, offset: Int) {
        
    }

    override fun glTexCoordPointer(size: Int, type: Int, stride: Int, pointer: Buffer?) {
        
    }

    override fun glFrustumf(left: Float, right: Float, bottom: Float, top: Float, zNear: Float, zFar: Float) {
        
    }

    override fun glStencilOp(fail: Int, zfail: Int, zpass: Int) {
        
    }

    override fun glAlphaFunc(func: Int, ref: Float) {
        
    }

    override fun glMaterialx(face: Int, pname: Int, param: Int) {
        
    }

    override fun glGenBuffers(n: Int, buffers: IntArray?, offset: Int) {
        
    }

    override fun glGenBuffers(n: Int, buffers: IntBuffer?) {
        
    }

    override fun glGetMaterialxv(face: Int, pname: Int, params: IntArray?, offset: Int) {
        
    }

    override fun glGetMaterialxv(face: Int, pname: Int, params: IntBuffer?) {
        
    }

    override fun glPopMatrix() {
        
    }

    override fun glReadPixels(x: Int, y: Int, width: Int, height: Int, format: Int, type: Int, pixels: Buffer?) {
        
    }

    override fun glStencilMask(mask: Int) {
        
    }

    override fun glIsTexture(texture: Int): Boolean {
        return false
    }

    override fun glFogxv(pname: Int, params: IntArray?, offset: Int) {
        
    }

    override fun glFogxv(pname: Int, params: IntBuffer?) {
        
    }

    override fun glMultiTexCoord4x(target: Int, s: Int, t: Int, r: Int, q: Int) {
        
    }

    override fun glSampleCoveragex(value: Int, invert: Boolean) {
        
    }

    override fun glGetString(name: Int): String {
        return ""
    }

    override fun glCompressedTexImage2D(target: Int, level: Int, internalformat: Int, width: Int, height: Int, border: Int, imageSize: Int, data: Buffer?) {
        
    }

    override fun glRotatex(angle: Int, x: Int, y: Int, z: Int) {
        
    }

    override fun glEnable(cap: Int) {
        
    }

    override fun glPointParameterf(pname: Int, param: Float) {
        
    }

    override fun glPolygonOffsetx(factor: Int, units: Int) {
        
    }

    override fun glClipPlanef(plane: Int, equation: FloatArray?, offset: Int) {
        
    }

    override fun glClipPlanef(plane: Int, equation: FloatBuffer?) {
        
    }

    override fun glTexEnvfv(target: Int, pname: Int, params: FloatArray?, offset: Int) {
        
    }

    override fun glTexEnvfv(target: Int, pname: Int, params: FloatBuffer?) {
        
    }

}