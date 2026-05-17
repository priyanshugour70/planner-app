import { apiDeleteJson, apiGetJson, apiPatchJson, apiPostJson } from "@/services/api/http-client";
import { NoteDTO } from "@/types/planner";

export const notesApi = {
  list: async (): Promise<NoteDTO[]> => {
    return apiGetJson<NoteDTO[]>("/planner/notes");
  },
  create: async (data: { title: string; body: string; color?: string | null; pinned?: boolean }): Promise<NoteDTO> => {
    return apiPostJson<NoteDTO>("/planner/notes", data);
  },
  update: async (id: string, data: Partial<NoteDTO>): Promise<NoteDTO> => {
    return apiPatchJson<NoteDTO>(`/planner/notes/${id}`, data);
  },
  delete: async (id: string): Promise<void> => {
    await apiDeleteJson<void>(`/planner/notes/${id}`);
  },
};
