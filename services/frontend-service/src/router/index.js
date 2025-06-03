import { createRouter, createWebHistory } from 'vue-router';
import UserLogin from '@/components/UserLogin.vue';
import UserRegister from '@/components/UserRegister.vue';
import PhotoUpload from '@/components/PhotoUploader.vue';
import Home from '@/components/Home.vue';
import YoloModel from '@/components/YoloModel.vue';
import MobileNetClassifier from '@/components/MobileNetClassifier.vue'; // Импортируем новый компонент

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home,
  },
  {
    path: '/login',
    name: 'UserLogin',
    component: UserLogin,
  },
  {
    path: '/register',
    name: 'UserRegister',
    component: UserRegister,
  },
  {
    path: '/upload',
    name: 'PhotoUpload',
    component: PhotoUpload,
  },
  {
    path: '/yolomodel',
    name: 'yoloModel',
    component: YoloModel,
  },
  {
    path: '/mobilenet',
    name: 'MobileNetClassifier',
    component: MobileNetClassifier,
    meta: {
      title: 'Image Classification with MobileNet' // Дополнительные метаданные
    }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/',
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

// Опционально: установка заголовков страниц
router.beforeEach((to, from, next) => {
  document.title = to.meta.title || 'Default Title';
  next();
});

export default router;