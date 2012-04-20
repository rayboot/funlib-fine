#include <stdio.h>    
#include <stdlib.h>
#include <jni.h>
#include <android/log.h>
#include <math.h>
#include "curve.h"

#define LOG_TAG "--imagefilter--"

#ifdef __DEBUG__
#define LOGE(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#else
#define LOGE(...)
#endif

#define max(a,b) (a>b)?(a):(b)
#define min(a,b) (a>b)?(b):(a) 

static __inline__ int ps_normalFun(int src, int mask , float maskAlpha){
	
	if(mask == 0) 
		return src;
	else
		return ((int) (maskAlpha * src + (1 - maskAlpha) * mask));
}

static __inline__ int ps_lightenFun(int src, int mask , float maskAlpha){
	src = mask > src ? mask : src;
	return ((int) (maskAlpha * mask + (1 - maskAlpha) * src));
}

static __inline__ int ps_darkenFun(int src, int mask , float maskAlpha) {
	src = mask > src ? src : mask;
	return ((int) (maskAlpha * mask + (1 - maskAlpha) * src));
}

static __inline__ int ps_multiplyFun(int src, int mask , float maskAlpha) {
	
	src = mask * src / 255;
	return ((int) (maskAlpha * mask + (1 - maskAlpha) * src));
}

static __inline__ int ps_averageFun(int src, int mask , float maskAlpha) {
	src = (src + mask) >> 1;
	return ((int) (maskAlpha * mask + (1 - maskAlpha) * src));
}

static __inline__ int ps_addFun(int src, int mask , float maskAlpha) {
	src = (src + mask > 255) ? 255 : src + mask;
	return ((int) (maskAlpha * mask + (1 - maskAlpha) * src));
}

static __inline__ int ps_subtractFun(int src, int mask , float maskAlpha) {
	src = (src + mask < 255) ? 0 : (src + mask - 255);
	return ((int) (maskAlpha * mask + (1 - maskAlpha) * src));
}

static __inline__ int ps_differenceFun(int src, int mask , float maskAlpha) {
	src = (src > mask) ? (src - mask) : (mask - src);
	return ((int) (maskAlpha * mask + (1 - maskAlpha) * src));
}

static __inline__ int ps_negationFun(int src, int mask , float maskAlpha) {
	src = (src + mask > 255) ? (src + mask) : (255 + 255 - src - mask);
	return ((int) (maskAlpha * mask + (1 - maskAlpha) * src));
}

static __inline__ int ps_screenFun(int src, int mask , float maskAlpha) {
	src = 255 - (((255 - src) * (255 - mask)) >> 8);
	return ((int) (maskAlpha * mask + (1 - maskAlpha) * src));
}

static __inline__ int ps_ExclusionFun(int src, int mask , float maskAlpha) {
	src = ((int)(src + mask - 2 * src * mask / 255));
	return ((int) (maskAlpha * mask + (1 - maskAlpha) * src));
}

static __inline__ int ps_overlayFun(int src, int mask , float maskAlpha) {
	src = ((int) ((mask < 128) ? (2 * src * mask / 255) : (255 - 2
				* (255 - src) * (255 - mask) / 255)));
	return ((int) (maskAlpha * mask + (1 - maskAlpha) * src));
}

static __inline__ int ps_softlightFun(int src, int mask , float maskAlpha) {

	src = (int) (((src < 128) ? (2 * ((mask >> 1) + 64))
				* ((float) src / 255) : (255 - (2 * (255 - ((mask >> 1) + 64))
				* (float) (255 - src) / 255))));
	return ((int) (maskAlpha * mask + (1 - maskAlpha) * src));
}

static __inline__ int ps_hardlightFun(int src, int mask , float maskAlpha) {
	return (ps_overlayFun(mask, src , maskAlpha));
}

static __inline__ int ps_ColorDodgeFun(int src, int mask , float maskAlpha) {

	src = ((int) ((mask == 255) ? mask : min(255,
				((src << 8) / (255 - mask)))));
	return ((int) (maskAlpha * mask + (1 - maskAlpha) * src));
}

static __inline__ int ps_ColorBurnFun(int src, int mask , float maskAlpha) {
	src = ((int) ((mask == 0) ? mask : max(0,
				(255 - ((255 - src) << 8) / mask))));
	return ((int) (maskAlpha * mask + (1 - maskAlpha) * src));
}

static __inline__ int ps_LinearDodgeFun(int src, int mask , float maskAlpha) {
	return ps_addFun(src, mask,maskAlpha);
}

static __inline__ int ps_LinearBurnFun(int src, int mask , float maskAlpha) {
	return ps_subtractFun(src, mask,maskAlpha);
}

static __inline__ int ps_LinearLightFun(int src, int mask , float maskAlpha) {
	return ((int) ((mask < 128) ? ps_LinearBurnFun(src, (2 * mask),maskAlpha)
				: ps_LinearDodgeFun(src, (2 * (mask - 128)),maskAlpha)));
}

static __inline__ int ps_VividLightFun(int src, int mask , float maskAlpha) {
	return ((int) ((mask < 128) ? ps_ColorBurnFun(src, (2 * mask),maskAlpha)
				: ps_ColorDodgeFun(src, (2 * (mask - 128)),maskAlpha)));
}

static __inline__ int ps_PinLightFun(int src, int mask , float maskAlpha) {
	return ((int) ((mask < 128) ? ps_darkenFun(src, (2 * mask),maskAlpha)
				: ps_lightenFun(src, (2 * (mask - 128)),maskAlpha)));
}

static __inline__ int ps_HardMixFun(int src, int mask , float maskAlpha) {
	return ((int) ((ps_VividLightFun(src, mask,maskAlpha) < 128) ? 0 : 255));
}

static __inline__ int ps_ReflectFun(int src, int mask , float maskAlpha) {
	src = ((int) ((mask == 255) ? mask : min(255,
				(src * src / (255 - mask)))));
	return ((int) (maskAlpha * mask + (1 - maskAlpha) * src));
}

static __inline__ int ps_GlowFun(int src, int mask , float maskAlpha) {
	return ps_ReflectFun(mask, src,maskAlpha);
}

static __inline__ int ps_PhoenixFun(int src, int mask , float maskAlpha) {
	src = ((int) (min(src, mask) - max(src, mask) + 255));
	return ((int) (maskAlpha * mask + (1 - maskAlpha) * src));
}

static __inline__ int ps_DevideFun(int src, int mask , float maskAlpha) {
	src = min(255, (int)(src/((float)(mask)/255)));
	return ((int) (maskAlpha * mask + (1 - maskAlpha) * src));
}

/*
enum EffectType {
		ChannelBlend_Normal=0, 
		ChannelBlend_Lighten, 
		ChannelBlend_Darken, 
		ChannelBlend_Multiply,
		ChannelBlend_Average,
		ChannelBlend_Add,
		ChannelBlend_Subtract,
		ChannelBlend_Difference,
		ChannelBlend_Negation,
		ChannelBlend_Screen,
		ChannelBlend_Exclusion,
		ChannelBlend_Overlay,
		ChannelBlend_SoftLight,
		ChannelBlend_HardLight,
		ChannelBlend_ColorDodge,
		ChannelBlend_ColorBurn,
		ChannelBlend_LinearDodge,
		ChannelBlend_LinearBurn,
		ChannelBlend_LinearLight,
		ChannelBlend_VividLight,
		ChannelBlend_PinLight,
		ChannelBlend_HardMix,
		ChannelBlend_Reflect,
		ChannelBlend_Glow,
		ChannelBlend_Phoenix,
		ChannelBlend_Devide,
	};
	
typedef int(*EffectFun)(int src , int mask , float maskAlpha);
EffectFun effectFun_options[] = {
	&ps_normalFun,
	&ps_lightenFun,
	&ps_darkenFun,
	&ps_multiplyFun,
	&ps_averageFun,
	&ps_addFun,
	&ps_subtractFun,
	&ps_differenceFun,
	&ps_negationFun,
	&ps_screenFun,
	&ps_ExclusionFun,
	&ps_overlayFun,
	&ps_softlightFun,
	&ps_hardlightFun,
	&ps_ColorDodgeFun,
	&ps_ColorBurnFun,
	&ps_LinearDodgeFun,
	&ps_LinearBurnFun,
	&ps_LinearLightFun,
	&ps_VividLightFun,
	&ps_PinLightFun,
	&ps_HardMixFun,
	&ps_ReflectFun,
	&ps_GlowFun,
	&ps_PhoenixFun,
	&ps_DevideFun
};
*/

static __inline__ int clamp(int rgb){

	rgb = max(0,rgb);
	return min(255,rgb);
}

static __inline__ int ALPHA(int rgb){
	return rgb >> 24 & 0xff;
}
static __inline__ int RED(int rgb){
	return rgb >> 16 & 0xff;
}
static __inline__ int GREEN(int rgb){
	return rgb >> 8 & 0xff;
}
static __inline__ int BLUE(int rgb){
	return rgb & 0xff;
}
static __inline__ int ARGB(int a , int r , int g , int b){
	a = clamp(a);
	r = clamp(r);
	g = clamp(g);
	b = clamp(b);
	
	return 	a << 24 | r << 16 | g << 8 | b;
}

//反相
static __inline__ int invertRGB(int rgb){

	int a = rgb & 0xff000000;
	return a | (~rgb & 0x00ffffff);
}

//grayscale
static __inline__ int grayScale(int rgb){
	
	int a = ALPHA(rgb);
    int r = RED(rgb);
    int g = GREEN(rgb);
    int b = BLUE(rgb);
	return (r * 0.3 + g * 0.59 + b * 0.11);
}

//高斯模糊
static __inline__ void gaussianBlur(jint* rgbBuf , int w , int h, int radius){

	int x,y,i,j;
	int index;
	int rgb,a,r,g,b;
	int squared = 4 * radius * radius + 4 * radius + 1;
	
	for (y = radius; y < (h - radius); y++)
		for (x = radius; x < (w - radius); x++)
		{
			int sumr = 0;
			int sumg = 0;
			int sumb = 0;
			
			for (j = -1 * radius; j <= radius; j++){
				for (i = -1 * radius; i <= radius; i++)
				{
					index = w*(y+j) + x + i;
					rgb = rgbBuf[index];
					a = ALPHA(rgb);
					sumr += RED(rgb);
					sumg += GREEN(rgb);
					sumb +=  BLUE(rgb);
				}
			}
			
			index = w*y + x;
			r = sumr / squared;
			g = sumg / squared;
			b = sumb / squared;
			r = clamp(r);
			g = clamp(g);
			b = clamp(b);
			rgbBuf[index] = ARGB(a , r , g , b); 
		}

	
}

//饱和度
static __inline__ int saturation1(int rgb , int level) {

		if ( level != 1 ) {
            int a = ALPHA(rgb);
            int r = RED(rgb);
            int g = GREEN(g);
            int b = BLUE(b);
            int v = ( r + g + b )/3; 
            r = clamp( (int)(v + level * (r-v)) );
            g = clamp( (int)(v + level * (g-v)) );
            b = clamp( (int)(v + level * (b-v)) );
            return ARGB(a , r , g , b);
        }
        return rgb;
}
static __inline__ void saturation2(int* r , int* g , int* b , int level) {

		if ( level != 1 ) {
            int v = ( *r + *g + *b )/3; 
            *r = clamp( (int)(v + level * (*r-v)) );
            *g = clamp( (int)(v + level * (*g-v)) );
            *b = clamp( (int)(v + level * (*b-v)) );
        }
}

#ifdef __cplusplus
extern "C" {
#endif
	
int JNICALL Java_com_funlib_imagefilter_ImageFilter_nativeEffectColorMatrix(    
        JNIEnv* env, jobject obj , jintArray srcBuf, jintArray matrixBuf , int w , int h) { 
        
    jint *srcTmpBuf = env->GetIntArrayElements(srcBuf, 0);  
    jint *matrixTmpBuf = env->GetIntArrayElements(matrixBuf, 0);   
    int alpha = 0xFF << 24;
    int i = 0;
    int j = 0;
    int index = 0;
    int rgb;
    int a;
    int r,g,b;
    int nr,ng,nb;
    
    if (srcTmpBuf == NULL) {    
        return -1;  
    }

    for(i = 0 ; i < h ; ++i){
    	for(j = 0 ; j < w ; ++j){
    	
				index = w * i + j;
				rgb = srcTmpBuf[index];    
				a = ALPHA(rgb);
            	r = RED(rgb);
            	g = GREEN(g);
            	b = BLUE(b);
				            
    			a = matrixTmpBuf[0] * a + matrixTmpBuf[1] * r + matrixTmpBuf[2] * g + matrixTmpBuf[3] * b + matrixTmpBuf[4] * 255;
    			r = matrixTmpBuf[5] * a + matrixTmpBuf[6] * r + matrixTmpBuf[7] * g + matrixTmpBuf[8] * b + matrixTmpBuf[9] * 255;
    			g = matrixTmpBuf[10] * a + matrixTmpBuf[11] * r + matrixTmpBuf[12] * g + matrixTmpBuf[13] * b + matrixTmpBuf[14] * 255;
    			b = matrixTmpBuf[15] * a + matrixTmpBuf[16] * r + matrixTmpBuf[17] * g + matrixTmpBuf[18] * b + matrixTmpBuf[19] * 255;
    			
				srcTmpBuf[index] =  ARGB(a , r , g , b); 
			}
	}

    env->ReleaseIntArrayElements(srcBuf, srcTmpBuf, 0);
    env->ReleaseIntArrayElements(matrixBuf, matrixTmpBuf, 0);       
    return 0;    
}
#ifdef __cplusplus
}
#endif


#ifdef __cplusplus
extern "C" {
#endif
int JNICALL Java_com_funlib_imagefilter_ImageFilter_nativeEffectSketch(    
        JNIEnv* env, jobject obj , jintArray srcBuf,int w , int h) { 
        
    int alpha = 0xFF << 24;
    int i = 0;
    int j = 0;
    int index = 0;
     
    jint *srcTmpBuf = env->GetIntArrayElements(srcBuf, 0);  
    int srcSize = env->GetArrayLength(srcBuf);
    if (srcTmpBuf == NULL) {    
        return -1;  
    }
    
    int newSize = w * h;
	jint maskTmpBuf[newSize]; // 新图像像素值
	memcpy(maskTmpBuf , srcTmpBuf , newSize*sizeof(jint));
	
    for(i = 0 ; i < h ; ++i){
    	for(j = 0 ; j < w ; ++j){
    	
				index = w * i + j;
				int rgb1 = maskTmpBuf[index];
				int rgb2 =  srcTmpBuf[index];   
				rgb1 = invertRGB(rgb1);
				
				int a1 = ALPHA(rgb1);
    			int r1 = RED(rgb1);
    			int g1 = GREEN(rgb1);
    			int b1 = BLUE(rgb1);
    			
    			int a2 = ALPHA(rgb2);
    			int r2 = RED(rgb2);
    			int g2 = GREEN(rgb2);
    			int b2 = BLUE(rgb2);
    			
    			r2 = ps_ColorDodgeFun(r2 , r1 , 0);
    			g2 = ps_ColorDodgeFun(g2 , g1 , 0);
    			b2 = ps_ColorDodgeFun(b2 , b1 , 0);
				            
				srcTmpBuf[index] =  ARGB(a2 , r2 , g2, b2); 
			}
	}

    env->ReleaseIntArrayElements(srcBuf, srcTmpBuf, 0);
    return 0;
}
#ifdef __cplusplus
}
#endif

#ifdef __cplusplus
extern "C" {
#endif

int JNICALL Java_com_funlib_imagefilter_ImageFilter_nativeEffectEmboss(    
        JNIEnv* env, jobject obj , jintArray srcBuf , int w , int h) { 
        
    jint *srcTmpBuf = env->GetIntArrayElements(srcBuf, 0);  
    int alpha = 0xFF << 24;
    int i = 0;
    int j = 0;
    int index = 0;
    int rgb1,rgb2;
    int a;
    int r,g,b;
    
    if (srcTmpBuf == NULL) {    
        return -1;  
    }
    
    for(i = 1 ; i < h - 1 ; ++i){
    	for(j = 1 ; j < w - 1 ; ++j){
    	
				index = w * i + j;
				rgb1 = srcTmpBuf[index];    
				rgb2 = srcTmpBuf[index+1];    
				a = ALPHA(rgb1);
    			r = RED(rgb2) - RED(rgb1) + 127;
    			g = GREEN(rgb2) - GREEN(rgb1) + 127;
    			b = BLUE(rgb2) - BLUE(rgb1) + 127;
				            
				srcTmpBuf[index] = ARGB(a , r , g , b); 
			}
	}

    env->ReleaseIntArrayElements(srcBuf, srcTmpBuf, 0);
    return 0;    
}
#ifdef __cplusplus
}
#endif

#ifdef __cplusplus
extern "C" {
#endif
int JNICALL Java_com_funlib_imagefilter_ImageFilter_nativeEffectGrayscale(    
        JNIEnv* env, jobject obj , jintArray srcBuf , int w , int h) { 
        
    jint *srcTmpBuf = env->GetIntArrayElements(srcBuf, 0);  
    int alpha = 0xFF << 24;
    int i = 0;
    int j = 0;
    int index = 0;
    int rgb;
    int a,r,g,b;
    
    if (srcTmpBuf == NULL) {    
        return -1;  
    }
    
    for(i = 0 ; i < h ; ++i){
    	for(j = 0 ; j < w ; ++j){
    	
				index = w * i + j;
				rgb = srcTmpBuf[index];    
				int a = ALPHA(rgb);
				rgb = grayScale(rgb);
				srcTmpBuf[index] =  ARGB(a , rgb , rgb , rgb); 
			}
	}

    env->ReleaseIntArrayElements(srcBuf, srcTmpBuf, 0);
    return 0;    
}
#ifdef __cplusplus
}
#endif


#ifdef __cplusplus
extern "C" {
#endif

int JNICALL Java_com_funlib_imagefilter_ImageFilter_nativeEffectOld(    
        JNIEnv* env, jobject obj , jintArray srcBuf, jintArray maskBuf , int w , int h) { 
        
    jint *srcTmpBuf = env->GetIntArrayElements(srcBuf, 0);  
    jint *maskTmpBuf = env->GetIntArrayElements(maskBuf, 0);   
    int alpha = 0xFF << 24;
    int i = 0;
    int j = 0;
    int index = 0;
    int rgb;
    int a;
    int r,g,b;
    int nr,ng,nb;
    
	for(i = 0 ; i < h ; ++i){
    	for(j = 0 ; j < w ; ++j){
    	
				index = w * i + j;
				int rgb = srcTmpBuf[index];
				int rgb2 =  maskTmpBuf[index];
				
				int a = ALPHA(rgb);
				rgb = grayScale(rgb);
				rgb =  ARGB(a , rgb , rgb , rgb);
    			a = ALPHA(rgb);
    			int r = RED(rgb);
    			int g = GREEN(rgb);
    			int b = BLUE(rgb);
    			
    			saturation2(&r , &g , &b , 0);
    			r = ps_softlightFun(r , 220 , 0);
    			g = ps_softlightFun(g , 134 , 0);
    			b = ps_softlightFun(b , 49 , 0);
    			
    			int a2 = ALPHA(rgb2);
    			int r2 = RED(rgb2);
    			int g2 = GREEN(rgb2);
    			int b2 = BLUE(rgb2);
    			r = ps_DevideFun(r , r2 , 0);
    			g = ps_DevideFun(g , g2 , 0);
    			b = ps_DevideFun(b , b2 , 0);
				            
				srcTmpBuf[index] =  ARGB(a , r , g , b); 
			}
	}
	
    env->ReleaseIntArrayElements(srcBuf, srcTmpBuf, 0);
    env->ReleaseIntArrayElements(maskBuf, maskTmpBuf, 0);       
    return 0;    
}
#ifdef __cplusplus
}
#endif

#ifdef __cplusplus
extern "C" {
#endif
int JNICALL Java_com_funlib_imagefilter_ImageFilter_nativeAdjustChannel(    
        JNIEnv* env, jobject obj , jintArray srcBuf , int w , int h , jintArray ptBuf , int adjustChannel) { 
        
    jint *srcTmpBuf = env->GetIntArrayElements(srcBuf, 0);  
    jint *ptTmpBuf = env->GetIntArrayElements(ptBuf, 0); 
    int alpha = 0xFF << 24;
    int i = 0;
    int j = 0;
    int index = 0;
    int rgb;
    int a,r,g,b;
    uint8_t level[256] = {0};
    
    Point pt;
    vector<Point> pts;
    double precision = 1.0;
    bool isXcalibrated = true;
    int ptsCnt = 0;
    
    int ptSize = env->GetArrayLength(ptBuf);
    for(i = 0 ; i < ptSize ; ++i){
    	if(ptTmpBuf[i] == -1) continue;
    	if(i % 2 == 0)
    		pt.X = ptTmpBuf[i];
    	else{
    		pt.Y = ptTmpBuf[i];
    		
    		pts.push_back(pt);
    	}
    }
    ptsCnt = pts.size();
    
    getImageLevel(pts , ptsCnt , level , precision , isXcalibrated);
    for(i = 0 ; i < h ; ++i){
    	for(j = 0 ; j < w ; ++j){
    	
				index = w * i + j;
				rgb = srcTmpBuf[index];  
				a = ALPHA(rgb);
    			r = RED(rgb);
    			g = GREEN(rgb);
    			b = BLUE(rgb);  
    			
    			if(adjustChannel == CHANNEL_RGB){
                    r = level[r];
                    g = level[g];
                    b = level[b];
    			}else if(adjustChannel == CHANNEL_R){
    				r = level[r];
    			}else if(adjustChannel == CHANNEL_G){
    				g = level[g];
    			}else if(adjustChannel == CHANNEL_B){
    				b = level[b];
    			} 
    			srcTmpBuf[index] = ARGB(a , r , g , b); 
			}
	}

	env->ReleaseIntArrayElements(ptBuf, ptTmpBuf, 0);
    env->ReleaseIntArrayElements(srcBuf, srcTmpBuf, 0);
    return 0;    
}
#ifdef __cplusplus
}
#endif

static __inline__ void contrast(int* r , int* g , int* b , float value){
	
	value = (100.0f + value) / 100.0f;
    value *= value;
	float red = *r / 255.0f;
    float green = *g / 255.0f;
    float blue = *b / 255.0f;
    red = (((red - 0.5f) * value) + 0.5f) * 255.0f;
    green = (((green - 0.5f) * value) + 0.5f) * 255.0f;
    blue = (((blue - 0.5f) * value) + 0.5f) * 255.0f;
    
    *r = clamp(red);
    *g = clamp(green);
    *b = clamp(blue);
}
#ifdef __cplusplus
extern "C" {
#endif
int JNICALL Java_com_funlib_imagefilter_ImageFilter_nativeEffectAdjustContrast(    
        JNIEnv* env, jobject obj , jintArray srcBuf , float value , int w , int h) { 
        
    jint *srcTmpBuf = env->GetIntArrayElements(srcBuf, 0);  
    int alpha = 0xFF << 24;
    int i = 0;
    int j = 0;
    int index = 0;
    int rgb;
    int a,r,g,b;
    
    for(i = 0 ; i < h ; ++i){
    	for(j = 0 ; j < w ; ++j){
    	
				index = w * i + j;
				rgb = srcTmpBuf[index];  
				a = ALPHA(rgb);
    			r = RED(rgb);
    			g = GREEN(rgb);
    			b = BLUE(rgb);  
    			
    			contrast(&r , &g , &b , value);
    			srcTmpBuf[index] = ARGB(a , r , g , b); 
			}
	}

    env->ReleaseIntArrayElements(srcBuf, srcTmpBuf, 0);
    return 0;    
}
#ifdef __cplusplus
}
#endif

static __inline__ void brightness(int* r , int* g , int* b , float value){
	
	if(value < -255) value = -255;
	if(value > 255) value = 255;
	value = 1 + value / 255.0f;
	
    *r = clamp((*r)*value);
    *g = clamp((*g)*value);
    *b = clamp((*b)*value);
}
#ifdef __cplusplus
extern "C" {
#endif
int JNICALL Java_com_funlib_imagefilter_ImageFilter_nativeEffectAdjustBrightness(    
        JNIEnv* env, jobject obj , jintArray srcBuf , float value , int w , int h) { 
        
    jint *srcTmpBuf = env->GetIntArrayElements(srcBuf, 0);  
    int alpha = 0xFF << 24;
    int i = 0;
    int j = 0;
    int index = 0;
    int rgb;
    int a,r,g,b;
    
    for(i = 0 ; i < h ; ++i){
    	for(j = 0 ; j < w ; ++j){
    	
				index = w * i + j;
				rgb = srcTmpBuf[index];  
				a = ALPHA(rgb);
    			r = RED(rgb);
    			g = GREEN(rgb);
    			b = BLUE(rgb);  
    			
    			brightness(&r , &g , &b , value);
    			srcTmpBuf[index] = ARGB(a , r , g , b); 
			}
	}

    env->ReleaseIntArrayElements(srcBuf, srcTmpBuf, 0);
    return 0;    
}
#ifdef __cplusplus
}
#endif

static __inline__ void gamma(int* r , int* g , int* b , float value){
	
    *r = clamp(pow(*r , value));
    *g = clamp(pow(*g , value));
    *b = clamp(pow(*b , value));
}
#ifdef __cplusplus
extern "C" {
#endif
int JNICALL Java_com_funlib_imagefilter_ImageFilter_nativeEffectAdjustGamma(    
        JNIEnv* env, jobject obj , jintArray srcBuf , float value , int w , int h) { 
        
    jint *srcTmpBuf = env->GetIntArrayElements(srcBuf, 0);  
    int alpha = 0xFF << 24;
    int i = 0;
    int j = 0;
    int index = 0;
    int rgb;
    int a,r,g,b;
    
    for(i = 0 ; i < h ; ++i){
    	for(j = 0 ; j < w ; ++j){
    	
				index = w * i + j;
				rgb = srcTmpBuf[index];  
				a = ALPHA(rgb);
    			r = RED(rgb);
    			g = GREEN(rgb);
    			b = BLUE(rgb);  
    			
    			gamma(&r , &g , &b , value);
    			srcTmpBuf[index] = ARGB(a , r , g , b); 
			}
	}

    env->ReleaseIntArrayElements(srcBuf, srcTmpBuf, 0);
    return 0;    
}
#ifdef __cplusplus
}
#endif

#ifdef __cplusplus
extern "C" {
#endif
int JNICALL Java_com_funlib_imagefilter_ImageFilter_nativeEffectSunShine(    
        JNIEnv* env, jobject obj , jintArray srcBuf, jint w, jint h,jint centerX, jint centerY, jint radius, jint strength) { 
    
    jint * cbuf;
	cbuf = env->GetIntArrayElements(srcBuf, 0);

	radius = min(centerX, centerY);
	int i = 0;
	int j = 0;
	for (i = 0; i < w; i++) {
		for (j = 0; j < h; j++) {
			int curr_color = cbuf[j * w + i];

			int pixR = RED(curr_color);
			int pixG = GREEN(curr_color);
			int pixB = BLUE(curr_color);

			int newR = pixR;
			int newG = pixG;
			int newB = pixB;
			int distance = (int) ((centerY - i) * (centerY - i) + (centerX - j) * (centerX - j));
			if (distance < radius * radius)
			{
				int result = (int) (strength * (1.0 - sqrt((double)distance) / radius));
				newR = pixR + result;
				newG = pixG + result;
				newB = pixB + result;
			}

			int a = ALPHA(curr_color);
			int modif_color = ARGB(a, newR, newG, newB);
			cbuf[j * w + i] = modif_color;
		}
	}
	env->ReleaseIntArrayElements(srcBuf, cbuf, 0);
	return 0;
}
#ifdef __cplusplus
}
#endif

#ifdef __cplusplus
extern "C" {
#endif
jint JNICALL Java_com_funlib_imagefilter_ImageFilter_nativeEffectFangDaJing(    
        JNIEnv* env, jobject obj , jintArray buf, jint w, jint h,jint centerX, jint centerY, jint radius, jfloat multiple) { 
    
   	jint * cbuf;
	cbuf = env->GetIntArrayElements(buf, 0);
	int newSize = w * h;
	jint rbuf[newSize]; // 新图像像素值

	float xishu = multiple;
	int real_radius = (int)(radius / xishu);

	int i = 0, j = 0;
	for (i = 0; i < w; i++)
	{
		for (j = 0; j < h; j++)
		{
			int curr_color = cbuf[j * w + i];

			int pixR = RED(curr_color);
			int pixG = GREEN(curr_color);
			int pixB = BLUE(curr_color);
			int pixA = ALPHA(curr_color);

			int newR = pixR;
			int newG = pixG;
			int newB = pixB;
			int newA = pixA;

			int distance = (int) ((centerX - i) * (centerX - i) + (centerY - j) * (centerY - j));
			if (distance < radius * radius)
			{
				// 图像放大效果
				int src_x = (int)((float)(i - centerX) / xishu + centerX);
				int src_y = (int)((float)(j - centerY) / xishu + centerY);

				int src_color = cbuf[src_y * w + src_x];
				newR = RED(src_color);
				newG = GREEN(src_color);
				newB = BLUE(src_color);
				newA = ALPHA(src_color);
			}

			int modif_color = ARGB(newA, newR, newG, newB);
			rbuf[j * w + i] = modif_color;
		}
	}

	env->SetIntArrayRegion(buf, 0, newSize, rbuf);
	env->ReleaseIntArrayElements(buf, cbuf, 0);
	return 0;
}
#ifdef __cplusplus
}
#endif

#ifdef __cplusplus
extern "C" {
#endif
int JNICALL Java_com_funlib_imagefilter_ImageFilter_nativeEffectHaHaJing(    
        JNIEnv* env, jobject obj , jintArray buf, jint width, jint height,jint centerX, jint centerY, jint radius, jfloat multiple) { 
    
   	jint * cbuf;
		cbuf = env->GetIntArrayElements(buf, 0);
		int newSize = width * height;
	jint rbuf[newSize]; // 新图像像素值

		float xishu = multiple;
		int real_radius = (int)(radius / xishu);

		int i = 0, j = 0;
		for (i = 0; i < width; i++)
		{
			for (j = 0; j < height; j++)
			{
				int curr_color = cbuf[j * width + i];

				int pixR = RED(curr_color);
				int pixG = GREEN(curr_color);
				int pixB = BLUE(curr_color);
				int pixA = ALPHA(curr_color);

				int newR = pixR;
				int newG = pixG;
				int newB = pixB;
				int newA = pixA;

				int distance = (int) ((centerX - i) * (centerX - i) + (centerY - j) * (centerY - j));
				if (distance < radius * radius)
				{
					// 放大镜的凹凸效果
					int src_x = (int) ((float) (i - centerX) / xishu);
					int src_y = (int) ((float) (j - centerY) / xishu);
					src_x = (int)(src_x * (sqrt((double)distance) / real_radius));
					src_y = (int)(src_y * (sqrt((double)distance) / real_radius));
					src_x = src_x + centerX;
					src_y = src_y + centerY;

					int src_color = cbuf[src_y * width + src_x];
					newR = RED(src_color);
					newG = GREEN(src_color);
					newB = BLUE(src_color);
					newA = ALPHA(src_color);
				}

				int modif_color = ARGB(newA, newR, newG, newB);
				rbuf[j * width + i] = modif_color;
			}
		}

	env->SetIntArrayRegion(buf, 0, newSize, rbuf);
	env->ReleaseIntArrayElements(buf, cbuf, 0);
	return 0;
}
#ifdef __cplusplus
}
#endif