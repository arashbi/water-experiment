package com.Experiments;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;

public class WaterExperiment implements ApplicationListener {
	private OrthographicCamera camera;
	private SpriteBatch batch;
	private Texture backgroundTexture;
	private Sprite sprite;


	String vertexShader = 
			"attribute vec4 a_position;    \n"
			+ "attribute vec2 a_texCoord0;\n"
			+ "uniform mat4 u_worldView;\n"
			+ "varying vec4 v_color;" 
			+ "varying vec2 v_texCoords;"
			+ "void main()                  \n"
			+ "{                            \n"
			+ "   v_color = vec4(1, 1, 1, 1); \n"
			+ "   v_texCoords = a_texCoord0; \n"
			+ "   gl_Position =  u_worldView * a_position;  \n"
			+ "}                            \n";
	
	String fragmentShader = "#ifdef GL_ES\n"
			+ "precision mediump float;\n"
			+ "#endif\n"
			+ "varying vec4 v_color;\n"
			+ "varying vec2 v_texCoords;\n"
			+ "uniform sampler2D u_texture;\n"
			+ "uniform sampler2D u_texture2;\n"
			+ "uniform float timedelta;\n"
			+ "void main()                                  \n"
			+ "{                                            \n"
			+ "  vec2 displacement = texture2D(u_texture2, v_texCoords/6.0).xy;\n" //
			+ "  float t=v_texCoords.y +displacement.y*0.1-0.15+  (sin(v_texCoords.x * 30.0+timedelta) * 0.01); \n" //
			+ "  gl_FragColor = v_color * texture2D(u_texture, vec2(v_texCoords.x,t));\n"
			+ "}";

	String waterShaderString = "#ifdef GL_ES\n"
		    + "precision mediump float;\n"
			+ "#endif\n" 
			+ "varying vec4 v_color;\n"
			+ "varying vec2 v_texCoords;\n" 
			+ "uniform sampler2D u_texture;\n"
			+ "void main()                                  \n"
			+ "{                                            \n"
			+ "  gl_FragColor = v_color * texture2D(u_texture, v_texCoords);\n"
			+ "}";


	ShaderProgram shader;
	ShaderProgram waterShader;
	
	Matrix4 matrix;
	float time;

	Mesh waterMesh;
	
	private Texture waterTexture;
	private Texture waterDisplacement;

	@Override
	public void create() {
		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();

		camera = new OrthographicCamera(1, h / w);
		batch = new SpriteBatch();

		backgroundTexture = new Texture(Gdx.files.internal("data/background.png"));
		backgroundTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);

		waterTexture =new Texture(Gdx.files.internal("data/water.png"));
		waterTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);

		waterDisplacement = new Texture(Gdx.files.internal("data/waterdisplacement.png"));
		waterDisplacement.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		waterDisplacement.bind();
		matrix = new Matrix4();
		
		TextureRegion region = new TextureRegion(backgroundTexture, 0, 0, 512, 275);
		ShaderProgram.pedantic=false;
		
		shader = new ShaderProgram(vertexShader, fragmentShader);
		
		waterShader = new ShaderProgram(vertexShader, waterShaderString);
		waterShader.setUniformMatrix("u_projTrans", matrix);
		
		waterMesh = createQuad(-1, -1, 1, -1, 1, -0.3f, -1, -0.3f);

		//BACKGROUND SPRITE
		sprite = new Sprite(region);
		sprite.setSize(1f, 1.3f * sprite.getHeight() / sprite.getWidth());
		sprite.setOrigin(sprite.getWidth() / 2, sprite.getHeight() / 2);
		sprite.setPosition(-sprite.getWidth() / 2, -sprite.getHeight() / 2);
		
		time=1f;
	}

	@Override
	public void dispose() {
		batch.dispose();
		backgroundTexture.dispose();
		waterTexture.dispose();
		waterDisplacement.dispose();
	}

	@Override
	public void render() {
		
		float dt = Gdx.graphics.getDeltaTime();
		time += dt;
		float angle = time * (2 * MathUtils.PI);
		if (angle > (2 * MathUtils.PI))
			angle -= (2 * MathUtils.PI);

		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);

		
		//RENDER BACKGROUND 
		batch.begin();
        batch.draw(backgroundTexture,0,0);
//		batch.setShader(waterShader);
//		waterShader.setUniformMatrix("u_worldView", camera.combined);
//		sprite.draw(batch);
		batch.end();
		
		//RENDER WATER
		Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		Gdx.gl20.glEnable(GL20.GL_BLEND);
		waterTexture.bind(1);
		waterDisplacement.bind(2);

		shader.begin();
		shader.setUniformMatrix("u_worldView",  matrix);
		shader.setUniformi("u_texture", 1);
		shader.setUniformi("u_texture2", 2);
		shader.setUniformf("timedelta", -angle);
		waterMesh.render(shader, GL20.GL_TRIANGLE_FAN);
		shader.end();
		
	}



	public Mesh createQuad(float x1, float y1, float x2, float y2, float x3,
			float y3, float x4, float y4) {
		float[] verts = new float[20];
		int i = 0;

		verts[i++] = x1; // x1
		verts[i++] = y1; // y1
		verts[i++] = 0;
		verts[i++] = 1f; // u1
		verts[i++] = 1f; // v1

		verts[i++] = x2; // x2
		verts[i++] = y2; // y2
		verts[i++] = 0;
		verts[i++] = 0f; // u2
		verts[i++] = 1f; // v2

		verts[i++] = x3; // x3
		verts[i++] = y3; // y2
		verts[i++] = 0;
		verts[i++] = 0f; // u3
		verts[i++] = 0f; // v3

		verts[i++] = x4; // x4
		verts[i++] = y4; // y4
		verts[i++] = 0;
		verts[i++] = 1f; // u4
		verts[i++] = 0f; // v4

		Mesh mesh = new Mesh(true, 4, 0, // static mesh with 4 vertices and no
											// indices
				new VertexAttribute(Usage.Position, 3,
						ShaderProgram.POSITION_ATTRIBUTE), new VertexAttribute(
						Usage.TextureCoordinates, 2,
						ShaderProgram.TEXCOORD_ATTRIBUTE + "0"));

		mesh.setVertices(verts);
		return mesh;

	}

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}
}
