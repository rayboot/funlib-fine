#include <stdio.h>    
#include <stdlib.h>
#include <jni.h>
#include <android/log.h>
#include <android/bitmap.h>

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

static jintArray resultBitmapMemory = NULL;
void JNICALL Java_com_funlib_imagefilter_ImageFilter_nativeAllocResultBitmapMemory(    
        JNIEnv* env, jobject obj, int size) { 
        
    resultBitmapMemory = (*env)->NewGlobalRef(env , (*env)->NewIntArray(env , size));
}

void JNICALL Java_com_funlib_imagefilter_ImageFilter_nativeFreeResultBitmapMemory(    
        JNIEnv* env, jobject obj) { 
        
    if(resultBitmapMemory != NULL){
    	
    	(*env)->DeleteGlobalRef(env, resultBitmapMemory);
    	resultBitmapMemory = NULL;
    }
}

static jintArray srcBitmapMemory = NULL;
jintArray JNICALL Java_com_funlib_imagefilter_ImageFilter_nativeAllocSrcBitmapMemory(    
        JNIEnv* env, jobject obj, int size) { 
        
    srcBitmapMemory = (*env)->NewGlobalRef(env , (*env)->NewIntArray(env , size));
}

void JNICALL Java_com_funlib_imagefilter_ImageFilter_nativeFreeSrcBitmapMemory(    
        JNIEnv* env, jobject obj) { 
        
    if(srcBitmapMemory != NULL){
    	
    	(*env)->DeleteGlobalRef(env, srcBitmapMemory);
    	srcBitmapMemory = NULL;
    }
}

static jintArray maskBitmapMemory = NULL;
jintArray JNICALL Java_com_funlib_imagefilter_ImageFilter_nativeAllocMaskBitmapMemory(    
        JNIEnv* env, jobject obj, int size) { 
        
    maskBitmapMemory = (*env)->NewGlobalRef(env , (*env)->NewIntArray(env , size));
}

void JNICALL Java_com_funlib_imagefilter_ImageFilter_nativeFreeMaskBitmapMemory(    
        JNIEnv* env, jobject obj) { 
        
    if(maskBitmapMemory != NULL){
    	
    	(*env)->DeleteGlobalRef(env, maskBitmapMemory);
    	maskBitmapMemory = NULL;
    }
}
        
jintArray JNICALL Java_com_funlib_imagefilter_ImageFilter_nativeGetEffectBitmapLower(    
        JNIEnv* env, jobject obj, int effectType , jintArray srcBuf,jintArray maskBuf, float maskAlpha, int w, int h) { 
    
    jint *srcTmpBuf = (*env)->GetIntArrayElements(env , srcBuf, 0);    
    jint *maskTmpBuf = (*env)->GetIntArrayElements(env , maskBuf, 0);    
    int alpha = 0xFF << 24;
    int i = 0;
    int j = 0;
    int size = w * h;   
    
    if (srcTmpBuf == NULL || maskTmpBuf == NULL) {    
        return NULL;  
    }
    
    for (i = 0; i < h; i++) {  
        for (j = 0; j < w; j++) {    
        
            int srcColor = srcTmpBuf[w * i + j];    
            int maskColor = maskTmpBuf[w * i + j];    
            
            //int srcAlpha = srcColor & 0x000000ff;
            //int maskAlpha = maskColor & 0x000000ff;
            
            int srcRed = ((srcColor & 0x00FF0000) >> 16);    
            int maskRed = ((maskColor & 0x00FF0000) >> 16);
            srcRed = effectFun_options[effectType](srcRed , maskRed , maskAlpha);
            
            int srcGreen = ((srcColor & 0x0000FF00) >> 8);   
            int maskGreen = ((maskColor & 0x0000FF00) >> 8);  
            srcGreen = effectFun_options[effectType](srcGreen , maskGreen , maskAlpha);
            
            int srcBlue = srcColor & 0x000000FF;
            int maskBlue = maskColor & 0x000000FF;
            srcBlue = effectFun_options[effectType](srcBlue , maskBlue , maskAlpha);
            
            srcTmpBuf[w * i + j] = alpha | (srcRed << 16) | (srcGreen << 8) | srcBlue;    
        }    
    }    

    (*env)->SetIntArrayRegion(env , resultBitmapMemory, 0, size, srcTmpBuf);
    (*env)->ReleaseIntArrayElements(env , srcBuf, srcTmpBuf, 0);    
    (*env)->ReleaseIntArrayElements(env , maskBuf, maskTmpBuf, 0);    
    
    return resultBitmapMemory;    
}

static int rgb_clamp(int value) {
  if(value > 255) {
    return 255;
  }
  if(value < 0) {
    return 0;
  }
  return value;
}
int JNICALL Java_com_funlib_imagefilter_ImageFilter_nativeGetEffectBitmapHigher(    
        JNIEnv* env, jobject obj, int effectType , jobject src , jobject mask , jobject result ,  float maskAlpha) { 

	AndroidBitmapInfo  srcInfo;
	AndroidBitmapInfo  maskInfo;
	AndroidBitmapInfo  resultInfo;
	void* srcPixel;
	void* maskPixel;
	void* resultPixel;
	int ret;
	int x , y;
	int w , h;
	int srcR , srcG , srcB;
	int maskR , maskG , maskB;
	int resultR , resultG , resultB;
	uint32_t* srcLine;
	uint32_t* maskLine;
	uint32_t* resultLine;

	if ((ret = AndroidBitmap_getInfo(env, src, &srcInfo)) < 0) {
		return -1;
     }
     if ((ret = AndroidBitmap_getInfo(env, mask, &maskInfo)) < 0) {
		return -1;
     }
     if ((ret = AndroidBitmap_getInfo(env, result, &resultInfo)) < 0) {
		return -1;
     }
     
     if ((ret = AndroidBitmap_lockPixels(env, src, &srcPixel)) < 0) {
     	return -1;
    }
    if ((ret = AndroidBitmap_lockPixels(env, mask, &maskPixel)) < 0) {
    	AndroidBitmap_unlockPixels(env, src);
     	return -1;
    }
    if ((ret = AndroidBitmap_lockPixels(env, result, &resultPixel)) < 0) {
    	AndroidBitmap_unlockPixels(env, src);
		AndroidBitmap_unlockPixels(env, mask);
     	return -1;
    }
    
    w = srcInfo.width;
    h = srcInfo.height;
    for(y = 0 ; y < h ; ++y){
    
    	srcLine = (uint32_t*)srcPixel;
    	maskLine = (uint32_t*)maskPixel;
    	resultLine = (uint32_t*)resultPixel;
    	for(x = 0 ; x < w ; ++x){

    		srcR = (int) ((srcLine[x] & 0x00FF0000) >> 16);
			srcG = (int)((srcLine[x] & 0x0000FF00) >> 8);
			srcB = (int) (srcLine[x] & 0x00000FF );
			maskR = (int) ((maskLine[x] & 0x00FF0000) >> 16);
			maskG = (int)((maskLine[x] & 0x0000FF00) >> 8);
			maskB = (int) (maskLine[x] & 0x00000FF );
			
			resultR = effectFun_options[effectType](srcR , maskR , maskAlpha);
			resultG = effectFun_options[effectType](srcG , maskG , maskAlpha);
			resultB = effectFun_options[effectType](srcB , maskB , maskAlpha);
			resultLine[x] = ((srcR << 16) & 0x00FF0000) |((srcG << 8) & 0x0000FF00) |(srcB & 0x000000FF);
    	}
    	
    	srcPixel = (char*)srcPixel + srcInfo.stride;
    	maskPixel = (char*)maskPixel + srcInfo.stride;
    	resultPixel = (char*)resultPixel + resultInfo.stride;
    }

	AndroidBitmap_unlockPixels(env, src);
	AndroidBitmap_unlockPixels(env, mask);
	AndroidBitmap_unlockPixels(env, result);
	return 0;
}
