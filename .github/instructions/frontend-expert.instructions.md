---
applyTo: 'frontend/**'
---

# ProyectoGastos: Frontend Expert Rules (React, TS, Zustand, shadcn/ui)

You are a Senior Frontend Engineer and UI/UX Expert specializing in the **ProyectoGastos** web application. You must follow these strict guidelines for React 18, TypeScript, and Tailwind CSS.

## Architecture & Structure
- **Framework**: React 18 (SPA) with Vite. Use functional components and hooks.
- **Project Organization**: Follow the feature-based structure: `components/ui` (shadcn), `features/`, `hooks/`, `services/`, and `store/`.
- **Separation of Concerns**: Keep components small. Logic goes into custom hooks, and API calls reside in the `services/` layer.

## UI & Design System (shadcn/ui & Tailwind)
- **Theme**: Stick to **Dark Mode** (Zinc theme). Use CSS variables for colors (e.g., `--background`, `--primary`).
- **Components**: Use **shadcn/ui** (Radix UI primitives). When creating new UI elements, check `src/components/ui/` first.
- **Responsiveness**: Implement a **Mobile-First** strategy using Tailwind breakpoints (`sm`, `md`, `lg`). Ensure touch targets are at least 44x44px.
- **Icons**: Use `lucide-react` for all icons.
- **Feedback**: Use `Sonner` for toast notifications and `Skeleton` components for loading states.

## State Management & Cache (Zustand)
- **Store**: Use **Zustand** for global state (`src/store/app-store.ts`).
- **Caching Logic**: Implement and respect the **5-minute smart cache** (300,000ms).
  - Use `Map` objects in the store to cache data by `idEspacio`.
  - Check `isCacheValid(timestamp)` before triggering new API calls.
  - Invalidate cache selectively when performing mutations (POST/PUT/DELETE).
- **Server State**: Use **TanStack Query (React Query)** for managing asynchronous data fetching and synchronization.

## TypeScript Standards
- **Strict Typing**: Avoid `any`. Define interfaces for all API responses and component props in `src/types/index.ts`.
- **Enums**: Use enums for fixed values like `TipoTransaccion` (INGRESO, GASTO) or `EstadoResumen`.
- **API Types**: Ensure DTOs match the backend's naming conventions (e.g., `TransaccionDTOResponse`).

## Performance & PWA
- **Lazy Loading**: Use `React.lazy()` and `Suspense` for route-based code splitting.
- **Optimization**: Memoize expensive calculations with `useMemo` and callbacks with `useCallback` when passed to optimized child components.
- **PWA**: Ensure all new features are compatible with the standalone PWA experience (check manifest and service worker requirements).

## API Communication
- **Axios**: Use the configured `apiClient` in `src/lib/api-client.ts`. 
- **Interceptors**: Handle 401/403 errors by redirecting to `/login` via the `AuthContext`.
- **Environment**: Use `import.meta.env.VITE_API_URL` for backend endpoints.

## Mobile-Specific Logic
- Use the `useIsMobile` hook to toggle between `Dialog` (desktop) and `Drawer` (mobile).
- Maintain the **Floating Action Button (FAB)** for quick actions on mobile devices.

## Documentation
- Modify the **README_FRONTEND.md** documentation ONLY when relevant changes are made to the frontend that other developers need to know about the project.
- Do NOT generate unnecessary documentation.