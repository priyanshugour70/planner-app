import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { calendarApi } from "@/modules/calendar/api/calendar-api";

export const calendarKeys = {
  all: ["calendar"] as const,
  events: (from?: string, to?: string) => [...calendarKeys.all, "events", { from, to }] as const,
  summaries: (month: string) => [...calendarKeys.all, "summary", month] as const,
};

export function useCalendarEvents(from?: string, to?: string) {
  return useQuery({
    queryKey: calendarKeys.events(from, to),
    queryFn: () => calendarApi.getEvents(from, to),
  });
}

export function useCalendarDailySummary(month: string) {
  return useQuery({
    queryKey: calendarKeys.summaries(month),
    queryFn: () => calendarApi.getDailySummary(month),
    enabled: !!month,
  });
}

export function useCreateCalendarEvent() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: calendarApi.createEvent,
    onSuccess: (_, variables) => {
      // Invalidate events
      void queryClient.invalidateQueries({ queryKey: calendarKeys.all });
    },
  });
}

export function useDeleteCalendarEvent() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: calendarApi.deleteEvent,
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: calendarKeys.all });
    },
  });
}
