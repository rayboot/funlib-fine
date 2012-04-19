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

static __inline__ int clamRGB(int rgb){

	rgb = max(0,rgb);
	return min(255,rgb);
}

//反相
static __inline__ int invertRGB(int rgb){

	int a = rgb & 0xff000000;
	return a | (~rgb & 0x00ffffff);
}

//grayscale
static __inline__ int grayScale(int rgb){
	
	int a = rgb >> 24 & 0xFF;
    int r = rgb >> 16 & 0xFF;
    int g = rgb >> 8 & 0xFF;
    int b = rgb & 0xFF;
	return (r * 77 + g * 151 + b * 28) >> 8;
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
					a = rgb >> 24 & 0xFF;
					sumr += rgb >> 16 & 0xFF;
					sumg += rgb >> 8 & 0xFF;;
					sumb +=  rgb&0xFF;
				}
			}
			
			index = w*y + x;
			r = sumr / squared;
			g = sumg / squared;
			b = sumb / squared;
			r = clamRGB(r);
			g = clamRGB(g);
			b = clamRGB(b);
			rgbBuf[index] = a << 24 | r << 16 | g << 8 | b; 
		}

	
}

//饱和度
static __inline__ int saturation1(int rgb , int level) {

		if ( level != 1 ) {
            int a = rgb & 0xff000000;
            int r = (rgb >> 16) & 0xff;
            int g = (rgb >> 8) & 0xff;
            int b = rgb & 0xff;
            int v = ( r + g + b )/3; 
            r = clamRGB( (int)(v + level * (r-v)) );
            g = clamRGB( (int)(v + level * (g-v)) );
            b = clamRGB( (int)(v + level * (b-v)) );
            return a | (r << 16) | (g << 8) | b;
        }
        return rgb;
}
static __inline__ void saturation2(int* r , int* g , int* b , int level) {

		if ( level != 1 ) {
            int v = ( *r + *g + *b )/3; 
            *r = clamRGB( (int)(v + level * (*r-v)) );
            *g = clamRGB( (int)(v + level * (*g-v)) );
            *b = clamRGB( (int)(v + level * (*b-v)) );
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
				a = rgb >> 24 & 0xFF;
    			r = rgb >> 16 & 0xFF;
    			g = rgb >> 8 & 0xFF;
    			b = rgb & 0xFF;
				            
    			a = matrixTmpBuf[0] * a + matrixTmpBuf[1] * r + matrixTmpBuf[2] * g + matrixTmpBuf[3] * b + matrixTmpBuf[4] * 255;
    			r = matrixTmpBuf[5] * a + matrixTmpBuf[6] * r + matrixTmpBuf[7] * g + matrixTmpBuf[8] * b + matrixTmpBuf[9] * 255;
    			g = matrixTmpBuf[10] * a + matrixTmpBuf[11] * r + matrixTmpBuf[12] * g + matrixTmpBuf[13] * b + matrixTmpBuf[14] * 255;
    			b = matrixTmpBuf[15] * a + matrixTmpBuf[16] * r + matrixTmpBuf[17] * g + matrixTmpBuf[18] * b + matrixTmpBuf[19] * 255;
    			
    			a = clamRGB(a);
    			r = clamRGB(r);
    			g = clamRGB(g);
    			b = clamRGB(b);
				srcTmpBuf[index] =  a << 24 | r << 16 | g << 8 | b; 
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
    
    jintArray maskBuf = env->NewIntArray(srcSize);
    if(maskBuf == NULL){
    	env->ReleaseIntArrayElements(srcBuf, srcTmpBuf, 0);
    	return -1;
    }
    env->SetIntArrayRegion(maskBuf, 0, srcSize, srcTmpBuf);
    jint *maskTmpBuf = env->GetIntArrayElements(maskBuf, 0);  
    for(i = 0 ; i < h ; ++i){
    	for(j = 0 ; j < w ; ++j){
    	
				index = w * i + j;
				int rgb1 = maskTmpBuf[index];
				int rgb2 =  srcTmpBuf[index];   
				rgb1 = invertRGB(rgb1);
				
				int a1 = rgb1 >> 24 & 0xFF;
    			int r1 = rgb1 >> 16 & 0xFF;
    			int g1 = rgb1 >> 8 & 0xFF;
    			int b1 = rgb1 & 0xFF;
    			
    			int a2 = rgb2 >> 24 & 0xFF;
    			int r2 = rgb2 >> 16 & 0xFF;
    			int g2 = rgb2 >> 8 & 0xFF;
    			int b2 = rgb2 & 0xFF;
    			
    			r2 = ps_ColorDodgeFun(r2 , r1 , 0);
    			g2 = ps_ColorDodgeFun(g2 , g1 , 0);
    			b2 = ps_ColorDodgeFun(b2 , b1 , 0);
				            
				a2 = clamRGB(a2);
				r2 = clamRGB(r2);
				g2 = clamRGB(g2);
				b2 = clamRGB(b2);
				srcTmpBuf[index] =  a2 << 24 | r2 << 16 | g2 << 8 | b2; 
			}
	}

	env->ReleaseIntArrayElements(maskBuf, maskTmpBuf, 0);
	env->DeleteLocalRef(maskBuf);
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
				a = rgb1 >> 24 & 0xFF;
    			r = rgb2 >> 16 & 0xFF - rgb1 >> 16 & 0xFF + 127;
    			g = rgb2 >> 8 & 0xFF - rgb1 >> 8 & 0xFF + 127;
    			b = rgb2 & 0xFF - rgb1 & 0xFF + 127;
				            
    			a = clamRGB(a);
    			r = clamRGB(r);
    			g = clamRGB(g);
    			b = clamRGB(b);
				srcTmpBuf[index] =  a << 24 | r << 16 | g << 8 | b; 
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
				rgb = grayScale(rgb);
				srcTmpBuf[index] =  a << 24 | rgb << 16 | rgb << 8 | rgb; 
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
    
    int srcSize = env->GetArrayLength(srcBuf);
    jintArray tmpBuf1 = env->NewIntArray(srcSize);
    if(tmpBuf1 == NULL){
    	env->ReleaseIntArrayElements(srcBuf, srcTmpBuf, 0);
    	env->ReleaseIntArrayElements(maskBuf, maskTmpBuf, 0);
    	return -1;
    }
    env->SetIntArrayRegion(tmpBuf1, 0, srcSize, srcTmpBuf);
    jint *tmpBuf11 = env->GetIntArrayElements(tmpBuf1, 0);  
    
    for(i = 0 ; i < h ; ++i){
    	for(j = 0 ; j < w ; ++j){
    	
				index = w * i + j;
				rgb = tmpBuf11[index];    
				rgb = grayScale(rgb);
				tmpBuf11[index] =  a << 24 | rgb << 16 | rgb << 8 | rgb; 
			}
	}
	
	//高斯模糊
	gaussianBlur(tmpBuf11 , w , h , 2);
	
	for(i = 0 ; i < h ; ++i){
    	for(j = 0 ; j < w ; ++j){
    	
				index = w * i + j;
				rgb = tmpBuf11[index];
				a = rgb >> 24 & 0xFF;
    			r = rgb >> 16 & 0xFF;
    			g = rgb >> 8 & 0xFF;
    			b = rgb & 0xFF;
    			
    			r = ps_softlightFun(r , 252 , 0);
    			g = ps_softlightFun(g , 125 , 0);
    			b = ps_softlightFun(b , 0 , 0);
				            
				r = clamRGB(r);
				g = clamRGB(g);
				b = clamRGB(b);
				tmpBuf11[index] =  a << 24 | r << 16 | g << 8 | b; 
			}
	}
	
	for(i = 0 ; i < h ; ++i){
    	for(j = 0 ; j < w ; ++j){
    	
				index = w * i + j;
				int rgb1 = srcTmpBuf[index];
				int rgb2 =  tmpBuf11[index];   
				
				int a1 = rgb1 >> 24 & 0xFF;
    			int r1 = rgb1 >> 16 & 0xFF;
    			int g1 = rgb1 >> 8 & 0xFF;
    			int b1 = rgb1 & 0xFF;
    			
    			int a2 = rgb2 >> 24 & 0xFF;
    			int r2 = rgb2 >> 16 & 0xFF;
    			int g2 = rgb2 >> 8 & 0xFF;
    			int b2 = rgb2 & 0xFF;
    			
    			saturation2(&r1 , &g1 , &b1 , 0);
    			r1 = ps_softlightFun(r1 , r2 , 0);
    			g1 = ps_softlightFun(g1 , g2 , 0);
    			b1 = ps_softlightFun(b1 , b2 , 0);
				            
				a1 = clamRGB(a1);
				r1 = clamRGB(r1);
				g1 = clamRGB(g1);
				b1 = clamRGB(b1);
				srcTmpBuf[index] =  a1 << 24 | r1 << 16 | g1 << 8 | b1; 
			}
	}
	
	for(i = 0 ; i < h ; ++i){
    	for(j = 0 ; j < w ; ++j){
    	
				index = w * i + j;
				int rgb1 = srcTmpBuf[index];
				int rgb2 =  maskTmpBuf[index];   
				
				int a1 = rgb1 >> 24 & 0xFF;
    			int r1 = rgb1 >> 16 & 0xFF;
    			int g1 = rgb1 >> 8 & 0xFF;
    			int b1 = rgb1 & 0xFF;
    			
    			int a2 = rgb2 >> 24 & 0xFF;
    			int r2 = rgb2 >> 16 & 0xFF;
    			int g2 = rgb2 >> 8 & 0xFF;
    			int b2 = rgb2 & 0xFF;
    			
    			r1 = ps_DevideFun(r1 , r2 , 0);
    			g1 = ps_DevideFun(g1 , g2 , 0);
    			b1 = ps_DevideFun(b1 , b2 , 0);
				            
				a1 = clamRGB(a1);
				r1 = clamRGB(r1);
				g1 = clamRGB(g1);
				b1 = clamRGB(b1);
				srcTmpBuf[index] =  a1 << 24 | r1 << 16 | g1 << 8 | b1; 
			}
	}
	

	env->ReleaseIntArrayElements(tmpBuf1, tmpBuf11, 0);
	env->DeleteLocalRef(tmpBuf1);
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
				a = rgb >> 24 & 0xFF;
    			r = rgb >> 16 & 0xFF;
    			g = rgb >> 8 & 0xFF;
    			b = rgb & 0xFF;  
    			
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
    			a = clamRGB(a);
    			g = clamRGB(g);
    			b = clamRGB(b);
    			r = clamRGB(r);
    			srcTmpBuf[index] = a << 24 | r << 16 | g << 8 | b; 
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
    
    *r = clamRGB(red);
    *g = clamRGB(green);
    *b = clamRGB(blue);
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
				a = rgb >> 24 & 0xFF;
    			r = rgb >> 16 & 0xFF;
    			g = rgb >> 8 & 0xFF;
    			b = rgb & 0xFF;  
    			
    			contrast(&r , &g , &b , value);
    			srcTmpBuf[index] = a << 24 | r << 16 | g << 8 | b; 
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
	
    *r = clamRGB((*r)*value);
    *g = clamRGB((*g)*value);
    *b = clamRGB((*b)*value);
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
				a = rgb >> 24 & 0xFF;
    			r = rgb >> 16 & 0xFF;
    			g = rgb >> 8 & 0xFF;
    			b = rgb & 0xFF;  
    			
    			brightness(&r , &g , &b , value);
    			srcTmpBuf[index] = a << 24 | r << 16 | g << 8 | b; 
			}
	}

    env->ReleaseIntArrayElements(srcBuf, srcTmpBuf, 0);
    return 0;    
}
#ifdef __cplusplus
}
#endif

static __inline__ void gamma(int* r , int* g , int* b , float value){
	
    *r = clamRGB(pow(*r , value));
    *g = clamRGB(pow(*g , value));
    *b = clamRGB(pow(*b , value));
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
				a = rgb >> 24 & 0xFF;
    			r = rgb >> 16 & 0xFF;
    			g = rgb >> 8 & 0xFF;
    			b = rgb & 0xFF;  
    			
    			gamma(&r , &g , &b , value);
    			srcTmpBuf[index] = a << 24 | r << 16 | g << 8 | b; 
			}
	}

    env->ReleaseIntArrayElements(srcBuf, srcTmpBuf, 0);
    return 0;    
}
#ifdef __cplusplus
}
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
    
    int srcSize = env->GetArrayLength(srcBuf);
    jintArray tmpBuf1 = env->NewIntArray(srcSize);
    if(tmpBuf1 == NULL){
    	env->ReleaseIntArrayElements(srcBuf, srcTmpBuf, 0);
    	env->ReleaseIntArrayElements(maskBuf, maskTmpBuf, 0);
    	return -1;
    }
    env->SetIntArrayRegion(tmpBuf1, 0, srcSize, srcTmpBuf);
    jint *tmpBuf11 = env->GetIntArrayElements(tmpBuf1, 0);  
    
    for(i = 0 ; i < h ; ++i){
    	for(j = 0 ; j < w ; ++j){
    	
				index = w * i + j;
				rgb = tmpBuf11[index];    
				rgb = grayScale(rgb);
				tmpBuf11[index] =  a << 24 | rgb << 16 | rgb << 8 | rgb; 
			}
	}
	
	//高斯模糊
	gaussianBlur(tmpBuf11 , w , h , 2);
	
	for(i = 0 ; i < h ; ++i){
    	for(j = 0 ; j < w ; ++j){
    	
				index = w * i + j;
				rgb = tmpBuf11[index];
				a = rgb >> 24 & 0xFF;
    			r = rgb >> 16 & 0xFF;
    			g = rgb >> 8 & 0xFF;
    			b = rgb & 0xFF;
    			
    			r = ps_softlightFun(r , 252 , 0);
    			g = ps_softlightFun(g , 125 , 0);
    			b = ps_softlightFun(b , 0 , 0);
				            
				r = clamRGB(r);
				g = clamRGB(g);
				b = clamRGB(b);
				tmpBuf11[index] =  a << 24 | r << 16 | g << 8 | b; 
			}
	}
	
	for(i = 0 ; i < h ; ++i){
    	for(j = 0 ; j < w ; ++j){
    	
				index = w * i + j;
				int rgb1 = srcTmpBuf[index];
				int rgb2 =  tmpBuf11[index];   
				
				int a1 = rgb1 >> 24 & 0xFF;
    			int r1 = rgb1 >> 16 & 0xFF;
    			int g1 = rgb1 >> 8 & 0xFF;
    			int b1 = rgb1 & 0xFF;
    			
    			int a2 = rgb2 >> 24 & 0xFF;
    			int r2 = rgb2 >> 16 & 0xFF;
    			int g2 = rgb2 >> 8 & 0xFF;
    			int b2 = rgb2 & 0xFF;
    			
    			saturation2(&r1 , &g1 , &b1 , 0);
    			r1 = ps_softlightFun(r1 , r2 , 0);
    			g1 = ps_softlightFun(g1 , g2 , 0);
    			b1 = ps_softlightFun(b1 , b2 , 0);
				            
				a1 = clamRGB(a1);
				r1 = clamRGB(r1);
				g1 = clamRGB(g1);
				b1 = clamRGB(b1);
				srcTmpBuf[index] =  a1 << 24 | r1 << 16 | g1 << 8 | b1; 
			}
	}
	
	for(i = 0 ; i < h ; ++i){
    	for(j = 0 ; j < w ; ++j){
    	
				index = w * i + j;
				int rgb1 = srcTmpBuf[index];
				int rgb2 =  maskTmpBuf[index];   
				
				int a1 = rgb1 >> 24 & 0xFF;
    			int r1 = rgb1 >> 16 & 0xFF;
    			int g1 = rgb1 >> 8 & 0xFF;
    			int b1 = rgb1 & 0xFF;
    			
    			int a2 = rgb2 >> 24 & 0xFF;
    			int r2 = rgb2 >> 16 & 0xFF;
    			int g2 = rgb2 >> 8 & 0xFF;
    			int b2 = rgb2 & 0xFF;
    			
    			r1 = ps_DevideFun(r1 , r2 , 0);
    			g1 = ps_DevideFun(g1 , g2 , 0);
    			b1 = ps_DevideFun(b1 , b2 , 0);
				            
				a1 = clamRGB(a1);
				r1 = clamRGB(r1);
				g1 = clamRGB(g1);
				b1 = clamRGB(b1);
				srcTmpBuf[index] =  a1 << 24 | r1 << 16 | g1 << 8 | b1; 
			}
	}
	

	env->ReleaseIntArrayElements(tmpBuf1, tmpBuf11, 0);
	env->DeleteLocalRef(tmpBuf1);
    env->ReleaseIntArrayElements(srcBuf, srcTmpBuf, 0);
    env->ReleaseIntArrayElements(maskBuf, maskTmpBuf, 0);       
    return 0;    
}
#ifdef __cplusplus
}
#endif