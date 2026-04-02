export type TicketPriority = "Urgent" | "High" | "Medium" | "Low";

export type TicketStatus =
  | "Received"
  | "Reviewing"
  | "Assigned"
  | "Working"
  | "Inspecting"
  | "Done";

export type Ticket = {
  id: string;
  title: string;
  summary: string;
  propertyName: string;
  unitLabel: string;
  tenantName: string;
  assignee: string;
  priority: TicketPriority;
  status: TicketStatus;
  requestedAt: string;
  dueLabel: string;
  estimatedCost: string;
  actualCost?: string;
  category: "Repair" | "Complaint" | "Document" | "Settlement";
  tags: string[];
};

export type TicketActivity = {
  id: string;
  actor: string;
  action: string;
  timestamp: string;
};

export type TicketChecklistItem = {
  id: string;
  label: string;
  done: boolean;
};

export type TicketDetail = Ticket & {
  description: string;
  evidence: string[];
  checklist: TicketChecklistItem[];
  activities: TicketActivity[];
  internalNotes: string[];
};

