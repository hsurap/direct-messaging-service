package com.parush.messaging_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponse<T> {
  private List<T> items;
  private String nextCursor;   // pass this back to get the next page; null if no more
  private boolean hasMore;
}
