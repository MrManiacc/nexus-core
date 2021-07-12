package nexus.plugins.opengl.data

import nexus.engine.render.Shader.ShaderSource
import nexus.engine.render.Shader.Type

/*Stores a group of named shader sources.*/
object Shaders {

    /*Compiles a simple shader that has it's version appended based upon what's passed in     */
    fun simple(
        version: String = "330",
        core: Boolean = true
    ): Pair<ShaderSource, ShaderSource> {
        return prefixVersion(
            version, core, ShaderSource(
                Type.Vertex,
                """
                layout(location = 0) in vec3 a_Pos; //Imports the position in 3d space (relative to mesh origin) of this vertex.
                
                uniform mat4 u_ViewProjection; //Calculations done on cpu side
                uniform mat4 u_ModelMatrix;
                
                out vec3 v_Pos;
                
                void main(){
                    gl_Position = u_ViewProjection * u_ModelMatrix * vec4(a_Pos, 1.0);                    
                    v_Pos = a_Pos;         
                }
            """.trimIndent()
            ) to ShaderSource(
                Type.Fragment,
                """
                layout(location = 0) out vec4 color; //Imports the position in 3d space (relative to mesh origin) of this vertex.
                
                uniform vec3 u_Camera;
                in vec3 v_Pos;
                
                void main(){
                    color = vec4(0.6534,0.254,0.2321, 1.0);
                }
            """.trimIndent()
            )
        )
    }

    /*A simple shader that has it's version appended based upon what's passed in*/
    fun flatShader(
        version: String = "330",
        core: Boolean = true
    ): Pair<ShaderSource, ShaderSource> {
        return prefixVersion(
            version, core, ShaderSource(
                Type.Vertex,
                """
                layout(location = 0) in vec3 a_Pos; //Imports the position in 3d space (relative to mesh origin) of this vertex.
                
                uniform mat4 u_ViewProjection; //Calculations done on cpu side
                uniform mat4 u_ModelMatrix;
                   
                out vec3 v_Pos;
                
                void main(){
                    gl_Position = u_ViewProjection * u_ModelMatrix * vec4(a_Pos, 1.0);                    
                    v_Pos = a_Pos;         
                } 
            """.trimIndent()
            ) to ShaderSource(
                Type.Fragment,
                """
                layout(location = 0) out vec4 color; //Imports the position in 3d space (relative to mesh origin) of this vertex.
                in vec3 v_Pos;
                
                uniform vec3 u_Color;
                
                void main(){
                    color = vec4(u_Color, 1.0);
                }
            """.trimIndent()
            )
        )
    }


    /*A simple shader that has it's version appended based upon what's passed in*/
    fun textureShader(
        version: String = "330",
        core: Boolean = true
    ): Pair<ShaderSource, ShaderSource> {
        return prefixVersion(
            version, core, ShaderSource(
                Type.Vertex,
                """
                layout(location = 0) in vec3 a_Pos; //Imports the position in 3d space (relative to mesh origin) of this vertex.
                layout(location = 1) in vec2 a_TexCord; 
                
                out vec2 v_TexCoord;
                
                uniform mat4 u_ViewProjection; //Calculations done on cpu side
                uniform mat4 u_ModelMatrix;
                
                void main(){
                    gl_Position = u_ViewProjection * u_ModelMatrix * vec4(a_Pos, 1.0);                    
                    v_TexCoord = a_TexCord;
                } 
            """.trimIndent()
            ) to ShaderSource(
                Type.Fragment,
                """
                layout(location = 0) out vec4 color; 
                in vec2 v_TexCoord;
                uniform sampler2D u_Texture;
                            
                void main(){
//                    color = vec4(v_TexCoord, 0.0, 1.0);    
                    color = texture(u_Texture, v_TexCoord);
                }
            """.trimIndent()
            )
        )
    }

    private fun prefixVersion(
        version: String,
        core: Boolean,
        sourceIn: Pair<ShaderSource, ShaderSource>
    ): Pair<ShaderSource, ShaderSource> {
        return ShaderSource(
            sourceIn.first.type,
            "#version $version ${if (core) "core" else ""}\n${sourceIn.first.source}"
        ) to ShaderSource(
            sourceIn.second.type,
            "#version $version ${if (core) "core" else ""}\n${sourceIn.second.source}"
        )
    }

}