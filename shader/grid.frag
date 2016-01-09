#version 330

in vec2 vertInPosition;
out vec4 outColor;
uniform float viewSettings;
uniform float viewIntensity;
uniform sampler2D texture;

// Used by Edge and Sharpness
vec4 get_pixel(in vec2 coords, in float dx, in float dy) {
    return texture2D(texture, coords + vec2(dx, dy));
}

/* This is all for Edge detection (link: coding-experiments.blogspot.cz/2010/06/edge-detection.html) */
float threshold(in float thr1, in float thr2 , in float val) {
    if (val < thr1) { return 0.0; }
    if (val > thr2) { return 1.0; }
    return val;
}

// averaged pixel intensity from 3 color channels
float avg_intensity(in vec4 pix) {
    return (pix.r + pix.g + pix.b)/3.;
}

// returns pixel color
float IsEdge(in vec2 coords, in float efectPower){
    float dxtex = 1.0 / float(textureSize(texture, 0)); // image width
    float dytex = 1.0 / float(textureSize(texture, 0)); // image height
    float pix[9];
    int k = -1;
    float delta;

    // read neighboring pixel intensities
    for (int i=-1; i<2; i++) {
        for(int j=-1; j<2; j++) {
            k++;
            pix[k] = avg_intensity( get_pixel(coords, float(i)*dxtex, float(j)*dytex) );
        }
    } 

  // average color differences around neighboring pixels
  delta = (abs(pix[1]-pix[7]) + abs(pix[5]-pix[3]) + abs(pix[0]-pix[8]) + abs(pix[2]-pix[6]))/4.;

  return threshold(efectPower,0.4,clamp(1.8*delta,0.0,1.0));
}
/* End of Edge detection */

/* This is for sharpness convultion - could be used for Gauss, Edge and Emboss either (link: coding-experiments.blogspot.cz/search/label/Sharpness) */
float[9] GetData(in vec2 coords, in int channel) {
    float dxtex = 1.0 / float(textureSize(texture,0)); // image width
    float dytex = 1.0 / float(textureSize(texture,0)); // image height
    float pix[9];
    int k = -1;

    // read neighboring pixel intensities
    for (int i=-1; i<2; i++) {
        for(int j=-1; j<2; j++) {
            k++;
            pix[k] = get_pixel(coords, float(i)*dxtex, float(j)*dytex)[channel];
        }
    }
    return pix;
}

float Convolve(in float[9] kernel, in float[9] matrix, in float denom, in float offset) {
   float res = 0.0;
   for (int i=0; i<9; i++) {
      res += kernel[i]*matrix[i];
   }
   return clamp(res/denom + offset,0.0,1.0);
}
/* End of sharpness */


/* Main function */
void main() {
    vec2 newPosition = (vertInPosition+1)/2; // Recalculating position to show on canvas (full width and height)

    if(viewSettings == 1){
        // Gauss blur convultion
        vec4 sum = vec4(0.0);

        float blurIntensity = 100 - viewIntensity;
        float gaussBlurSize = 1 / (blurIntensity*30);

        // Apply blurring, using a 9-tap filter with gaussBlurSize set by slider
        sum += texture2D(texture, vec2(newPosition.x - 4.0*gaussBlurSize, newPosition.y)) * 0.05;
        sum += texture2D(texture, vec2(newPosition.x - 3.0*gaussBlurSize, newPosition.y)) * 0.09;
        sum += texture2D(texture, vec2(newPosition.x - 2.0*gaussBlurSize, newPosition.y)) * 0.12;
        sum += texture2D(texture, vec2(newPosition.x - gaussBlurSize, newPosition.y)) * 0.15;
        sum += texture2D(texture, vec2(newPosition.x, newPosition.y)) * 0.16;
        sum += texture2D(texture, vec2(newPosition.x + gaussBlurSize, newPosition.y)) * 0.15;
        sum += texture2D(texture, vec2(newPosition.x + 2.0*gaussBlurSize, newPosition.y)) * 0.12;
        sum += texture2D(texture, vec2(newPosition.x + 3.0*gaussBlurSize, newPosition.y)) * 0.09;
        sum += texture2D(texture, vec2(newPosition.x + 4.0*gaussBlurSize, newPosition.y)) * 0.05;

        outColor = sum;

    } else if(viewSettings == 2) {
        // Edge detection convultion
        float col = IsEdge(newPosition, viewIntensity / 100);

        outColor = vec4(col, col, col, 1.0);

    } else if(viewSettings == 3) {
        // Emboss convultion
        float intensity = (100 - viewIntensity);
        if(intensity == 0) { intensity = 1; }
        float pixelWidth = 64 * intensity;  // view intensity to change pixel width and height (to be able to change by slider)
        float pixelheight = 48 * intensity;

        vec2 onePixel = vec2(1.0 / pixelWidth, 1.0 / pixelheight);	 
        vec2 texCoord = newPosition;

        vec4 color;
        color.rgb = vec3(0.5);
        color -= texture2D(texture, texCoord - onePixel) * 5.0;
        color += texture2D(texture, texCoord + onePixel) * 5.0;
        color.rgb = vec3((color.r + color.g + color.b) / 3.0);

        outColor = vec4(color.rgb, 1);

    } else if(viewSettings == 4) {
        // Sharpness convultion
        float[9] sharpness = float[] (-1.,-1.,-1.,
                                    -1., 9., -1.,
                                    -1., -1., -1.);

        float matR[9] = GetData(newPosition, 0);
        float matG[9] = GetData(newPosition, 1);
        float matB[9] = GetData(newPosition, 2);

        float intensity = 1.0 - (viewIntensity / 100);

        outColor = vec4(Convolve(sharpness, matR, intensity, 0.),
                        Convolve(sharpness, matG, intensity, 0.),
                        Convolve(sharpness, matB, intensity, 0.),1.0);

    } else if(viewSettings == 5) {
        // Grayscale

        vec2 pos = vec2(newPosition.s, newPosition.t);
        float r = texture2D(texture, pos).r;
        float g = texture2D(texture, pos).g;
        float b = texture2D(texture, pos).b;
        float col = (viewIntensity / 50*r)+(viewIntensity / 50*g)+(viewIntensity / 50*b);

        outColor = vec4(col, col, col, 1.0f);
   
    } else if(viewSettings == 6) {
        // Pixelization
        float intensity = (100 - viewIntensity);
        if(intensity == 0) { intensity = 1; }

        vec2 dimensions = vec2(intensity * 8, intensity * 6);
        vec2 size = vec2(1.0, 1.0) / dimensions;
        vec2 pixel = vec2(0.0);
        pixel.s = newPosition.s - mod(newPosition.s, size.s);
        pixel.t = newPosition.t - mod(newPosition.t, size.t);
        pixel += 0.5 * size;

        outColor = texture2D(texture, pixel);

    } else if(viewSettings == 7) {
        // Brightness
        float intensity = (100 - viewIntensity);
        vec4 defaultColor = texture2D(texture, newPosition);
        float brightness = (defaultColor.r + defaultColor.g + defaultColor.b) / (intensity / 5);

        outColor = vec4(brightness, brightness, brightness, defaultColor.a);

    } else {
        // Default - display normal picture
        outColor=texture2D(texture, newPosition);
    }
}