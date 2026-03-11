import adapter from '@sveltejs/adapter-static'; // adapter-auto에서 static으로 변경
import { vitePreprocess } from '@sveltejs/vite-plugin-svelte';

/** @type {import('@sveltejs/kit').Config} */
const config = {
  preprocess: vitePreprocess(),

  kit: {
    adapter: adapter({
      // 빌드 결과물이 저장될 폴더명 (Dockerfile의 경로와 일치해야 함)
      pages: 'build',
      assets: 'build',
      // SPA 라우팅을 위해 필수적인 설정입니다. 
      // 이 설정이 있어야 모든 경로에서 index.html을 찾을 수 있습니다.
      fallback: 'index.html', 
      precompress: false,
      strict: true
    })
  }
};

export default config;