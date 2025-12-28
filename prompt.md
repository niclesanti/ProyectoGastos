Para manejar el consumo de APIs de forma profesional en un stack moderno de **React 19**, la mejor práctica no es usar `useEffect` directamente, sino implementar **TanStack Query (React Query)** junto con **Axios**.

Esta combinación resuelve tu duda sobre "no solicitar a la base de datos continuamente": **TanStack Query** actúa como una capa de caché inteligente que mantiene los datos en memoria y solo los refresca cuando es necesario.

Aquí tienes la arquitectura profesional para implementar esto.

---

### 1. Stack de Comunicación y Estado

Para este flujo, utilizaremos:

* **Axios:** Para las peticiones HTTP (mejor manejo de interceptores y errores que `fetch`).
* **TanStack Query (v5):** Para el "Server State" (caché de la lista de espacios).
* **Zustand:** Para el "Client State" (saber qué espacio está seleccionado actualmente y los datos del usuario).

---

### 2. Definición de Interfaces (TypeScript)

Primero, creamos los "DTOs" en el frontend para que coincidan con tus Records de Java.

```typescript
// src/types/workspace.ts
export interface EspacioTrabajoResponse {
  id: number;
  nombre: string;
  saldo: number;
  usuarioAdminId: number;
}

export interface EspacioTrabajoRequest {
  nombre: string;
  idUsuarioAdmin: number;
}

```

---

### 3. Configuración del Cliente API y el Store

Configuramos Axios para que siempre envíe las credenciales (cookies de sesión) y Zustand para guardar el contexto del usuario.

```typescript
// src/lib/api-client.ts
import axios from 'axios';

export const apiClient = axios.create({
  baseURL: 'http://localhost:8080/api',
  withCredentials: true, // Vital para OAuth2/Session
});

// src/store/use-auth-store.ts
import { create } from 'zustand';

interface AuthState {
  user: { id: number; nombre: string; email: string } | null;
  currentWorkspaceId: number | null;
  setAuth: (user: any) => void;
  setCurrentWorkspaceId: (id: number) => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  user: null, // Se carga al autenticar
  currentWorkspaceId: null,
  setAuth: (user) => set({ user }),
  setCurrentWorkspaceId: (id) => set({ currentWorkspaceId: id }),
}));

```

---

### 4. Implementación de Hooks de TanStack Query

Aquí es donde ocurre la "magia" del caché. Si tres componentes necesitan la lista de espacios, solo se hará **una petición** a la base de datos.

```typescript
// src/features/workspaces/api/workspace-queries.ts
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '@/lib/api-client';
import { EspacioTrabajoResponse, EspacioTrabajoRequest } from '@/types/workspace';

// Hook para LISTAR (GET)
export const useWorkspaces = (userId: number | undefined) => {
  return useQuery({
    queryKey: ['workspaces', userId], // El caché se identifica por este ID
    queryFn: async () => {
      const { data } = await apiClient.get<EspacioTrabajoResponse[]>(`/espaciotrabajo/listar/${userId}`);
      return data;
    },
    enabled: !!userId, // Solo se ejecuta si hay un usuario autenticado
    staleTime: 1000 * 60 * 5, // Los datos se consideran "frescos" por 5 minutos
  });
};

// Hook para REGISTRAR (POST)
export const useCreateWorkspace = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (newWorkspace: EspacioTrabajoRequest) => {
      return await apiClient.post('/espaciotrabajo/registrar', newWorkspace);
    },
    onSuccess: () => {
      // Invalida el caché para que la sidebar se actualice automáticamente
      queryClient.invalidateQueries({ queryKey: ['workspaces'] });
    },
  });
};

```

---

### 5. Aplicación en los Componentes

#### En la Sidebar (Selector de Espacios)

```tsx
const { user } = useAuthStore();
const { data: workspaces, isLoading } = useWorkspaces(user?.id);

// En el selector de shadcn/ui:
{workspaces?.map((ws) => (
  <SelectItem key={ws.id} value={ws.id.toString()}>
    {ws.nombre} - ${ws.saldo}
  </SelectItem>
))}

```

#### En Configuración (Guardar nuevo espacio)

```tsx
const createWorkspace = useCreateWorkspace();
const { user } = useAuthStore();

const onSubmit = (data: { nombre: string }) => {
  createWorkspace.mutate({
    nombre: data.nombre,
    idUsuarioAdmin: user!.id
  });
};

```

---

### 6. Guía para GitHub Copilot

Para que Copilot implemente esta lógica de forma masiva, usa este prompt:

> **"Role: Senior Fullstack Developer. Implement the API consumption layer using TanStack Query v5 and Axios.**
> **1. API Client:** Setup an Axios instance in `src/lib/api-client.ts` with `baseURL` and `withCredentials: true`.
> **2. TypeScript Interfaces:** Create types for `EspacioTrabajoResponse` and `EspacioTrabajoRequest` based on the Java records provided.
> **3. Query Hooks:** >    - Create a `useWorkspaces(userId)` hook using `useQuery`. Set a `staleTime` of 5 minutes to manage cache professionally and avoid redundant database hits.
> * Create a `useCreateWorkspace()` hook using `useMutation`. On `onSuccess`, use `queryClient.invalidateQueries` to refresh the workspace list automatically.
> **4. State Management:** Use Zustand to store the `user` object and the `currentWorkspaceId`.
> **5. Integration:** >    - Update the Sidebar `WorkspaceSwitcher` to consume `useWorkspaces`.
> * Update the Settings 'Nuevo espacio de trabajo' form to use `useCreateWorkspace` when the 'Guardar' button is pressed.
> 
> 
> **Follow the architecture we've built using the Zinc dark theme and shadcn/ui components."**

---

### Por qué esta es la forma más profesional:

1. **Caché Automático:** No necesitas guardar la lista en un estado global manualmente. TanStack Query lo hace por ti y decide cuándo está "vieja" (stale) la información.
2. **Sincronización:** Al usar `invalidateQueries`, cuando creas un espacio en Configuración, el selector de la Sidebar se actualiza solo, sin recargar la página.
3. **Manejo de Estados:** Tienes acceso fácil a `isLoading`, `isError` y `isSuccess` para mostrar esqueletos o notificaciones (Toasts) al usuario.
