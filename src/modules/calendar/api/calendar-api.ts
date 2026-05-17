import { apiGetJson, apiPostJson, apiDeleteJson } from "@/services/api/http-client";
import type { CalendarEventDTO } from "@/types/planner";

export const calendarApi = {
  getEvents: async (from?: string, to?: string): Promise<CalendarEventDTO[]> => {
    let url = "/planner/calendar-events";
    const params: string[] = [];
    if (from) params.push(`from=${encodeURIComponent(from)}`);
    if (to) params.push(`to=${encodeURIComponent(to)}`);
    if (params.length > 0) url += `?${params.join("&")}`;
    
    return apiGetJson<CalendarEventDTO[]>(url);
  },

  createEvent: async (body: { title: string; startsAt: string; endsAt: string; location?: string; description?: string }): Promise<CalendarEventDTO> => {
    return apiPostJson<CalendarEventDTO>("/planner/calendar-events", body);
  },

  deleteEvent: async (id: string): Promise<void> => {
    await apiDeleteJson<{ ok: true }>(`/planner/calendar-events/${id}`);
  },

  getDailySummary: async (month: string): Promise<Record<string, {
    tasks: any[];
    habits: any[];
    journals: any[];
    notes: any[];
    transactions: any[];
    events: any[];
  }>> => {
    return apiGetJson<Record<string, {
      tasks: any[];
      habits: any[];
      journals: any[];
      notes: any[];
      transactions: any[];
      events: any[];
    }>>(`/planner/calendar/daily-summary?month=${encodeURIComponent(month)}`);
  }
};
