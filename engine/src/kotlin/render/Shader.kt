package marx.engine.render

import org.joml.*

/*
 * A shader is a program that resides on the gpu. It should be implementation agnostic. We will base this off t he opengl
 * implementation
 */
abstract class Shader {
    abstract val isValid: Boolean

    /*
   This should compile the shader.
     */
    abstract fun compile(sources: ShaderProgram): Boolean

    /*
Compiles given n number of shader sources
     */
    fun compile(vararg sources: ShaderSource): Boolean =
        compile(ShaderProgram(sources.asList()))

    /*
Compiles given n number of shader sources
     */
    fun compile(sources: Pair<ShaderSource, ShaderSource>): Boolean =
        compile(ShaderProgram(arrayListOf(sources.first, sources.second)))

    /*
   This should destroy the shader program. Called upon closing of a layer or the window/app
     */
    abstract fun destroy()

    /*
Bind the shader for writing to
     */
    abstract fun bind()

    /*
Unbind the shader, called when we are done writing.
     */
    abstract fun unbind()

    /*This should update a vec4 to the shader**/
    abstract fun updateVec4(uniform: String, vector: Vector4f)

    /*This should update a vec3 to the shader**/
    abstract fun uploadVec3(uniform: String, vector: Vector3f)

    /*This should update a vec2 to the shader**/
    abstract fun updateVec2(uniform: String, vector: Vector2f)

    /*This should update a float to the shader**/
    abstract fun uploadFloat(uniform: String, float: Float)

    /*This should update a float to the shader**/
    abstract fun uploadMat4(uniform: String, matrix: Matrix4f)

    /*This should update a float to the shader**/
    abstract fun uploadMat3(uniform: String, matrix: Matrix3f)

    /*
   This is a class that wraps shader's source code with an enum of their type,
and the source code it's self.
     */
    data class ShaderSource(
        val type: Type,
        var source: String,
        val result: CompileResult = CompileResult(type, CompileState.Uncompiled)
    )

    data class ShaderProgram(
        val sources: List<ShaderSource>
    )

    /*
Allows for a solid implementation of a shader type
     */
    enum class Type {
        Vertex, Fragment, Program
    }

    /*
   This is the result of a shader compilation process
     */
    data class CompileResult(
        val type: Type = Type.Vertex,
        var state: CompileState = CompileState.Uncompiled,
        var message: String = ""
    ) {
        val isValid: Boolean get() = state == CompileState.Compiled
    }

    /*Used to determine the current state of a shader**/
    enum class CompileState {
        Uncompiled, Compiled, Error, Compiling
    }
}