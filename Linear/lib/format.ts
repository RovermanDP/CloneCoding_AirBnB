import type { TicketPriority } from "@/types/ticket";

export function priorityClassName(priority: TicketPriority) {
  switch (priority) {
    case "Urgent":
      return "priority-urgent";
    case "High":
      return "priority-high";
    case "Medium":
      return "priority-medium";
    default:
      return "";
  }
}

