package com.foodflow.service.impl;

import com.foodflow.document.*;
import com.foodflow.dto.*;
import com.foodflow.enums.Role;
import com.foodflow.enums.WorkflowStatus;
import com.foodflow.exception.*;
import com.foodflow.mapper.WorkflowMapper;
import com.foodflow.repository.*;
import com.foodflow.service.DonationWorkflowService;
import com.foodflow.service.StatusTransitionService;
import com.foodflow.service.WorkflowValidationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DonationWorkflowServiceImpl implements DonationWorkflowService {

    private final AllocationWorkflowRepository workflowRepository;
    private final DonationAllocationRepository allocationRepository;
    private final DonationRepository donationRepository;
    private final NgoProfileRepository ngoProfileRepository;
    private final RestaurantProfileRepository restaurantProfileRepository;
    private final UserRepository userRepository;
    private final StatusTransitionService statusTransitionService;
    private final WorkflowValidationService workflowValidationService;
    private final WorkflowMapper workflowMapper;

    public DonationWorkflowServiceImpl(
            AllocationWorkflowRepository workflowRepository,
            DonationAllocationRepository allocationRepository,
            DonationRepository donationRepository,
            NgoProfileRepository ngoProfileRepository,
            RestaurantProfileRepository restaurantProfileRepository,
            UserRepository userRepository,
            StatusTransitionService statusTransitionService,
            WorkflowValidationService workflowValidationService,
            WorkflowMapper workflowMapper
    ) {
        this.workflowRepository = workflowRepository;
        this.allocationRepository = allocationRepository;
        this.donationRepository = donationRepository;
        this.ngoProfileRepository = ngoProfileRepository;
        this.restaurantProfileRepository = restaurantProfileRepository;
        this.userRepository = userRepository;
        this.statusTransitionService = statusTransitionService;
        this.workflowValidationService = workflowValidationService;
        this.workflowMapper = workflowMapper;
    }

    @Override
    public WorkflowResponse acceptAllocation(String email, String allocationId) {
        UserDocument user = getUser(email);
        if (user.getRole() != Role.NGO && user.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only NGOs can accept donation allocations");
        }

        NgoProfileDocument ngo = ngoProfileRepository.findByUserIdAndIsDeletedFalse(user.getId())
                .orElseThrow(() -> new NgoNotFoundException("NGO profile not found for user: " + email));

        DonationAllocationDocument allocation = allocationRepository.findById(allocationId)
                .orElseThrow(() -> new AllocationException("Allocation not found: " + allocationId));

        workflowValidationService.validateNgoAllocationAssignment(allocation, ngo.getId());

        AllocationWorkflowDocument workflow = workflowRepository
                .findByAllocationIdAndNgoId(allocationId, ngo.getId())
                .orElseGet(() -> AllocationWorkflowDocument.builder()
                        .allocationId(allocationId)
                        .donationId(allocation.getDonationId())
                        .restaurantId(allocation.getRestaurantId())
                        .ngoId(ngo.getId())
                        .currentStatus(WorkflowStatus.PUBLISHED)
                        .build());

        AllocationWorkflowDocument updated = statusTransitionService.transition(
                workflow, WorkflowStatus.NGO_ACCEPTED, email, "NGO accepted allocation slice"
        );

        AllocationWorkflowDocument saved = workflowRepository.save(updated);
        return workflowMapper.toResponse(saved);
    }

    @Override
    public WorkflowResponse confirmNgo(String email, String allocationId, String targetNgoId) {
        UserDocument user = getUser(email);
        DonationAllocationDocument allocation = allocationRepository.findById(allocationId)
                .orElseThrow(() -> new AllocationException("Allocation not found: " + allocationId));

        workflowValidationService.validateRestaurantOwnership(allocation.getRestaurantId(), user.getId(), user.getRole());

        AllocationWorkflowDocument workflow = workflowRepository
                .findByAllocationIdAndNgoId(allocationId, targetNgoId)
                .orElseThrow(() -> new WorkflowException("Workflow not found for allocation " + allocationId + " and NGO " + targetNgoId));

        AllocationWorkflowDocument updated = statusTransitionService.transition(
                workflow, WorkflowStatus.RESTAURANT_CONFIRMED, email, "Restaurant confirmed NGO"
        );

        AllocationWorkflowDocument saved = workflowRepository.save(updated);
        return workflowMapper.toResponse(saved);
    }

    @Override
    public WorkflowResponse schedulePickup(String email, String allocationId, String targetNgoId, SchedulePickupRequest request) {
        UserDocument user = getUser(email);
        DonationAllocationDocument allocation = allocationRepository.findById(allocationId)
                .orElseThrow(() -> new AllocationException("Allocation not found: " + allocationId));

        workflowValidationService.validateRestaurantOwnership(allocation.getRestaurantId(), user.getId(), user.getRole());
        workflowValidationService.validateFutureTime(request.getPickupScheduledTime());

        AllocationWorkflowDocument workflow = workflowRepository
                .findByAllocationIdAndNgoId(allocationId, targetNgoId)
                .orElseThrow(() -> new WorkflowException("Workflow not found for allocation " + allocationId + " and NGO " + targetNgoId));

        AllocationWorkflowDocument updated = statusTransitionService.transitionSchedulePickup(
                workflow, request.getPickupScheduledTime(), email
        );

        AllocationWorkflowDocument saved = workflowRepository.save(updated);
        return workflowMapper.toResponse(saved);
    }

    @Override
    public WorkflowResponse startPickup(String email, String allocationId) {
        UserDocument user = getUser(email);
        if (user.getRole() != Role.NGO && user.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only NGOs can start pickup");
        }

        NgoProfileDocument ngo = ngoProfileRepository.findByUserIdAndIsDeletedFalse(user.getId())
                .orElseThrow(() -> new NgoNotFoundException("NGO profile not found for user: " + email));

        AllocationWorkflowDocument workflow = workflowRepository
                .findByAllocationIdAndNgoId(allocationId, ngo.getId())
                .orElseThrow(() -> new WorkflowException("Workflow not found for allocation " + allocationId + " and NGO " + ngo.getId()));

        AllocationWorkflowDocument updated = statusTransitionService.transition(
                workflow, WorkflowStatus.PICKUP_STARTED, email, "Pickup started"
        );

        AllocationWorkflowDocument saved = workflowRepository.save(updated);
        return workflowMapper.toResponse(saved);
    }

    @Override
    public WorkflowResponse completePickup(String email, String allocationId) {
        UserDocument user = getUser(email);
        if (user.getRole() != Role.NGO && user.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only NGOs can complete pickup");
        }

        NgoProfileDocument ngo = ngoProfileRepository.findByUserIdAndIsDeletedFalse(user.getId())
                .orElseThrow(() -> new NgoNotFoundException("NGO profile not found for user: " + email));

        AllocationWorkflowDocument workflow = workflowRepository
                .findByAllocationIdAndNgoId(allocationId, ngo.getId())
                .orElseThrow(() -> new WorkflowException("Workflow not found for allocation " + allocationId + " and NGO " + ngo.getId()));

        AllocationWorkflowDocument updated = statusTransitionService.transition(
                workflow, WorkflowStatus.PICKED_UP, email, "Pickup completed"
        );

        AllocationWorkflowDocument saved = workflowRepository.save(updated);
        return workflowMapper.toResponse(saved);
    }

    @Override
    public WorkflowResponse completeDelivery(String email, String allocationId, DeliveryCompleteRequest request) {
        UserDocument user = getUser(email);
        if (user.getRole() != Role.NGO && user.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only NGOs can complete delivery");
        }

        NgoProfileDocument ngo = ngoProfileRepository.findByUserIdAndIsDeletedFalse(user.getId())
                .orElseThrow(() -> new NgoNotFoundException("NGO profile not found for user: " + email));

        AllocationWorkflowDocument workflow = workflowRepository
                .findByAllocationIdAndNgoId(allocationId, ngo.getId())
                .orElseThrow(() -> new WorkflowException("Workflow not found for allocation " + allocationId + " and NGO " + ngo.getId()));

        String remarks = request != null && request.getCompletionRemarks() != null
                ? request.getCompletionRemarks()
                : "Delivery completed";

        AllocationWorkflowDocument delivered = statusTransitionService.transition(
                workflow, WorkflowStatus.DELIVERED, email, remarks
        );

        AllocationWorkflowDocument completed = statusTransitionService.transition(
                delivered, WorkflowStatus.COMPLETED, email, "Workflow closed successfully"
        );

        AllocationWorkflowDocument saved = workflowRepository.save(completed);
        return workflowMapper.toResponse(saved);
    }

    @Override
    public WorkflowResponse cancelWorkflow(String email, String allocationId, String targetNgoId, CancelDonationRequest request) {
        UserDocument user = getUser(email);
        DonationAllocationDocument allocation = allocationRepository.findById(allocationId)
                .orElseThrow(() -> new AllocationException("Allocation not found: " + allocationId));

        String ngoIdToCancel = targetNgoId;
        if (user.getRole() == Role.NGO) {
            NgoProfileDocument ngo = ngoProfileRepository.findByUserIdAndIsDeletedFalse(user.getId())
                    .orElseThrow(() -> new NgoNotFoundException("NGO profile not found"));
            ngoIdToCancel = ngo.getId();
        } else if (user.getRole() == Role.RESTAURANT) {
            workflowValidationService.validateRestaurantOwnership(allocation.getRestaurantId(), user.getId(), user.getRole());
        }

        if (ngoIdToCancel == null) {
            throw new WorkflowException("Target NGO ID is required for cancellation");
        }

        final String finalNgoIdToCancel = ngoIdToCancel;
        AllocationWorkflowDocument workflow = workflowRepository
                .findByAllocationIdAndNgoId(allocationId, finalNgoIdToCancel)
                .orElseThrow(() -> new WorkflowException("Workflow not found for allocation " + allocationId + " and NGO " + finalNgoIdToCancel));

        AllocationWorkflowDocument updated = statusTransitionService.transition(
                workflow, WorkflowStatus.CANCELLED, email, request.getRemarks()
        );

        AllocationWorkflowDocument saved = workflowRepository.save(updated);
        return workflowMapper.toResponse(saved);
    }

    @Override
    public WorkflowResponse getWorkflowForNgo(String email, String allocationId) {
        UserDocument user = getUser(email);
        if (user.getRole() != Role.NGO && user.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only NGOs or Admins can view individual workflow slices");
        }

        String ngoId = null;
        if (user.getRole() == Role.NGO) {
            NgoProfileDocument ngo = ngoProfileRepository.findByUserIdAndIsDeletedFalse(user.getId())
                    .orElseThrow(() -> new NgoNotFoundException("NGO profile not found"));
            ngoId = ngo.getId();
        }

        final String finalNgoId = ngoId;
        AllocationWorkflowDocument doc = workflowRepository.findByAllocationIdAndNgoId(allocationId, finalNgoId)
                .orElseThrow(() -> new WorkflowException("Workflow not found for allocation: " + allocationId));

        return workflowMapper.toResponse(doc);
    }

    @Override
    public WorkflowResponse getWorkflowById(String email, String id) {
        UserDocument user = getUser(email);
        AllocationWorkflowDocument doc = workflowRepository.findById(id)
                .orElseThrow(() -> new WorkflowException("Workflow not found: " + id));

        if (user.getRole() == Role.NGO) {
            NgoProfileDocument ngo = ngoProfileRepository.findByUserIdAndIsDeletedFalse(user.getId())
                    .orElseThrow(() -> new NgoNotFoundException("NGO profile not found"));
            if (!doc.getNgoId().equals(ngo.getId())) {
                throw new AccessDeniedException("You are not authorized to view this workflow slice");
            }
        } else if (user.getRole() == Role.RESTAURANT) {
            workflowValidationService.validateRestaurantOwnership(doc.getRestaurantId(), user.getId(), user.getRole());
        }

        return workflowMapper.toResponse(doc);
    }

    @Override
    public Page<WorkflowResponse> getWorkflowHistory(String email, int page, int size) {
        UserDocument user = getUser(email);
        Pageable pageable = PageRequest.of(page, size);
        Page<AllocationWorkflowDocument> docs;

        if (user.getRole() == Role.ADMIN) {
            docs = workflowRepository.findAllByOrderByCreatedAtDesc(pageable);
        } else if (user.getRole() == Role.RESTAURANT) {
            RestaurantProfileDocument restaurant = restaurantProfileRepository.findByUserIdAndIsDeletedFalse(user.getId())
                    .orElseThrow(() -> new RestaurantNotFoundException("Restaurant profile not found"));
            docs = workflowRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurant.getId(), pageable);
        } else if (user.getRole() == Role.NGO) {
            NgoProfileDocument ngo = ngoProfileRepository.findByUserIdAndIsDeletedFalse(user.getId())
                    .orElseThrow(() -> new NgoNotFoundException("NGO profile not found"));
            docs = workflowRepository.findByNgoIdOrderByCreatedAtDesc(ngo.getId(), pageable);
        } else {
            throw new AccessDeniedException("Unauthorized role for workflow history");
        }

        return docs.map(workflowMapper::toResponse);
    }

    @Override
    public DonationWorkflowAggregateResponse getDonationAggregate(String email, String donationId) {
        UserDocument user = getUser(email);
        DonationDocument donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new DonationNotFoundException("Donation not found: " + donationId));

        if (user.getRole() == Role.RESTAURANT) {
            workflowValidationService.validateRestaurantOwnership(donation.getRestaurantId(), user.getId(), user.getRole());
        } else if (user.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only restaurants or admins can view donation workflow aggregates");
        }

        List<AllocationWorkflowDocument> workflows = workflowRepository.findByDonationId(donationId);
        return workflowMapper.toAggregateResponse(donationId, workflows);
    }

    private UserDocument getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }
}
