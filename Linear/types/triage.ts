import type { TicketPriority, TicketStatus } from "@/types/ticket";

export type TriageRequestStatus = "New" | "On Hold" | "Rejected" | "Converted";

export type TriageRequest = {
  id: string;
  title: string;
  summary: string;
  propertyName: string;
  unitLabel: string;
  tenantName: string;
  receivedAt: string;
  category: "Repair" | "Complaint" | "Document" | "Settlement";
  suggestedPriority: TicketPriority;
  attachments: number;
  tags: string[];
  status: TriageRequestStatus;
  recommendedStatus: TicketStatus;
};

